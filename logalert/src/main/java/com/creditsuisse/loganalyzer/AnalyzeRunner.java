package com.creditsuisse.loganalyzer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.creditsuisse.loganalyzer.db.DB;

/**
 * Runner for log analyzer.
 * Initiates thread pool and schedules workers.
 */
public class AnalyzeRunner {
    final static Logger logger = LoggerFactory.getLogger(AnalyzeRunner.class);

    public static class Builder {
        private int threadNumber = 1;
        private Optional<Integer> segmentCount = Optional.empty();
        private long timeAlertThreshold = 4;
        private File fileToAnalyze;
        private String dbName = DB.DEFAULT_DB_NAME;
        
        public Builder(File fileToAnalyze) {
            this.fileToAnalyze = fileToAnalyze;
        }
        
        public Builder withThreads(int threadNumber) {
            this.threadNumber = threadNumber;
            return this;
        }
        
        public Builder withSegments(int segmentCount) {
            this.segmentCount = Optional.of(segmentCount);
            return this;
        }
        
        public Builder withTimeThreshold(long timeAlertThreshold) {
            this.timeAlertThreshold = timeAlertThreshold;
            return this;
        }
        
        public Builder withDbName(String dbName) {
            this.dbName = dbName;
            return this;
        }
        
        public AnalyzeRunner build() {
            return new AnalyzeRunner(
                fileToAnalyze,
                threadNumber,
                segmentCount.isPresent() ?  segmentCount.get() : threadNumber,
                timeAlertThreshold,
                dbName
            );
        }
    }
    
    private int threadNumber;
    private int segmentCount;
    private long timeAlertThreshold;
    private File fileToAnalyze;
    private String dbName;

    public AnalyzeRunner(
            File fileToAnalyze, 
            int threadNumber, 
            int segmentCount, 
            long timeAlertThreshold,
            String dbName) {
        this.fileToAnalyze = fileToAnalyze;
        this.threadNumber = threadNumber;
        this.segmentCount = segmentCount;
        this.timeAlertThreshold = timeAlertThreshold;
        this.dbName = dbName;
    }
    
    public void analyzeFile() {
        ExecutorService executor = null;
        DB db = null;
        try {
            db = new DB();
            db.init(dbName);
            db.createDb();
            db.deleteRecords(); // TODO: could be some extra flag to clear database
            
            ResultsCollector globalCollector = new ResultsCollector(timeAlertThreshold, db);
            
            executor = Executors.newFixedThreadPool(threadNumber);
            executor.invokeAll(IntStream.range(0, segmentCount)
                .mapToObj(i -> createTask(i, globalCollector))
                .collect(Collectors.toList()));
            
            logger.info("All workers finished");
            db.countRecords();
            
        } catch (InterruptedException e) {
            logger.error("Interrupted exception", e);
        } catch (SQLException | IOException e) {
            logger.error("Exception while processing file", e);
        } finally {
            if (executor != null) {
                executor.shutdownNow();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    private AnalyzeTask createTask(int workerNumber, ResultsCollector collector) {
        AnalyzeTask task = new AnalyzeTask(workerNumber, segmentCount, timeAlertThreshold, fileToAnalyze, collector);
        task.setDbName(dbName);
        return task;
    }
}
