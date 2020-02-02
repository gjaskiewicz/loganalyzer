package com.creditsuisse.loganalyzer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.creditsuisse.loganalyzer.data.LogEntry;
import com.creditsuisse.loganalyzer.util.BufferedRandomAccessFileReader;
import com.creditsuisse.loganalyzer.util.IPartialFileReader;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.creditsuisse.loganalyzer.db.DB;
import com.creditsuisse.loganalyzer.db.ILogStore;

/**
 * Worker for analyzing log entries.
 * Uses random file access to read part of file.
 * Will read all json entries which begin in segment handled by given worker.
 * 
 * Important assumption is that '{' and '}' chars are not used as part of text fields for
 * logs and log itself has no nested objects.
 */
public class AnalyzeTask implements Callable<Void> {
    final static Logger logger = LoggerFactory.getLogger(AnalyzeTask.class);
    
    private int workerNumber;
    private int totalWorkers;
    private File inputFile;
    private long timeAlertThreshold;
   
    private ObjectMapper mapper;
    private ResultsCollector globalCollector;
    
    private Supplier<ILogStore> logStoreSupplier;
    // TODO: it would be better to have it in dependency injection framework, eg. Guice

    public AnalyzeTask(
            int workerNumber, 
            int totalWorkers, 
            long timeAlertThreshold, 
            File inputFile, 
            ResultsCollector globalCollector) {
        this.workerNumber = workerNumber;
        this.totalWorkers = totalWorkers;
        this.inputFile = inputFile;
        this.timeAlertThreshold = timeAlertThreshold;
        this.globalCollector = globalCollector;
        
        this.mapper = new ObjectMapper();
        this.logStoreSupplier = () -> provideDefaultDb();
    }
    
    private ILogStore provideDefaultDb() {
        DB db = new DB(); 
        try {
            db.init();
        } catch (SQLException e) {
            logger.error("Error while getting DB", e);
            return null;
        }
        return db;
    }
    
    public void setDbSupplier(Supplier<ILogStore> logStoreSupplier) {
        this.logStoreSupplier = logStoreSupplier;
    }

    @Override
    public Void call() throws Exception {
        RandomAccessFile file = null;
        ILogStore db = null;
        try {
            db = logStoreSupplier.get();
            file = new RandomAccessFile(inputFile, "r");
            ResultsCollector resultsCollector = new ResultsCollector(timeAlertThreshold, db);
            
            long fileLength = file.length();
            long segmentSize = fileLength / totalWorkers;
            
            long startIndex = segmentSize * workerNumber;
            long endIndex = isLastWorker() ? fileLength : segmentSize * (workerNumber + 1);

            logger.info("Worker {}/{} started with begin={} end={}", 
                workerNumber, totalWorkers, startIndex, endIndex);
            
            BufferedRandomAccessFileReader braf = new BufferedRandomAccessFileReader(file);
            processFile(braf, resultsCollector, fileLength, startIndex, endIndex);
            
            logger.info("Worker {} has {} unmapped log entries", workerNumber, resultsCollector.getEntriesCount());
            synchronized(globalCollector) {
                globalCollector.collect(resultsCollector);
            }
        } catch (IOException | SQLException e) {
            logger.error("Worker {} failed", workerNumber, e);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    logger.error("Error while closing the file", e);
                }
            }
            if (db != null) {
                db.close();
            }
        }
        
        return null;
    }

    // default access for test case
    void processFile(IPartialFileReader braf, ResultsCollector resultsCollector, long fileLength, long startIndex,
            long endIndex) throws IOException, SQLException {
        braf.seek(startIndex);
        while (braf.getFilePointer() < endIndex) {
            if (braf.skipUntilChar('{', endIndex) == false) {
                return;
            }
            braf.skip(-1);
            String jsonChunk = braf.readUntilChar('}', fileLength);
            if (jsonChunk == null) {
                return;
            }
            logger.debug("Worker {} - processing chunk {}", workerNumber, jsonChunk);
            LogEntry entry = tryParseLogEntry(jsonChunk);
            if (entry != null) {
                resultsCollector.collect(entry);
            }
        }
    }

    private LogEntry tryParseLogEntry(String jsonChunk) {
        try {
            return mapper.readValue(jsonChunk, LogEntry.class);
        } catch (IOException e) {
            logger.warn("Worker {} - could not parse chunk", workerNumber, e);
        }
        
        return null;
    }
    
    private boolean isLastWorker() {
        return workerNumber == (totalWorkers - 1);
    }
}
