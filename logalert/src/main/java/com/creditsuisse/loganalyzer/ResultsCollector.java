package com.creditsuisse.loganalyzer;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.creditsuisse.loganalyzer.data.LogDuration;
import com.creditsuisse.loganalyzer.data.LogEntry;
import com.creditsuisse.loganalyzer.data.LogEntry.State;

import com.creditsuisse.loganalyzer.db.ILogStore;

/**
 * Collector for results tries matching log records and writes them to Db.
 */
public class ResultsCollector {
    final static Logger logger = LoggerFactory.getLogger(ResultsCollector.class);
    
    private Map<String, LogEntry> entries;
    private long timeAlertThreshold;
    private ILogStore db;
    
    public ResultsCollector(long timeAlertThreshold, ILogStore db) {
        this.entries = new HashMap<>();
        this.timeAlertThreshold = timeAlertThreshold;
        this.db = db;
    }
    
    public void collect(LogEntry logEntry) throws SQLException {
        LogDuration duration = tryMatchLogDuration(logEntry);
        if (duration != null) {
            db.insert(duration);
        }
    }
    
    public LogDuration tryMatchLogDuration(LogEntry logEntry) {
        if (logEntry.id == null) {
            logger.warn("Found log entry without id");
            return null;
        }
        if (entries.containsKey(logEntry.id)) {
            return matchToLogDuration(entries.get(logEntry.id), logEntry);
        } else {
            entries.put(logEntry.id, logEntry);
            return null;
        }
    }
    
    private LogDuration matchToLogDuration(LogEntry logEntry1, LogEntry logEntry2) {
        if (logEntry1.state == logEntry2.state) {
            logger.warn("Log with id={} has duplicate state {}", logEntry1.id, logEntry1.state);
            return null;
        }
        // TODO: compare type and log warning on mismatch
        // TODO: compare host and log warning on mismatch
        
        entries.remove(logEntry1.id);
        long startTime = logEntry1.state == State.STARTED ? logEntry1.timestamp : logEntry2.timestamp;
        long endTime = logEntry1.state == State.FINISHED ? logEntry1.timestamp : logEntry2.timestamp;
        long timeDelta = endTime - startTime;
        
        LogDuration logDuration = new LogDuration();
        logDuration.id = logEntry1.id;
        logDuration.timeStarted = startTime;
        logDuration.timeEnded = endTime;
        logDuration.timeDelta = timeDelta;
        logDuration.type = logEntry1.type;
        logDuration.host = logEntry1.host;
        logDuration.alert = (timeDelta > timeAlertThreshold);
        
        if (timeDelta < 0) {
            logger.warn("Log with id={} has negative time {}", logEntry1.id, timeDelta);
        }
        
        return logDuration;
    }
    
    public int getEntriesCount() {
        return entries.size();
    }

    public void collect(ResultsCollector collector) {
        for (LogEntry log : collector.entries.values()) {
            try {
                collect(log);
            } catch (Exception e) {
                logger.error("Error while handling unmatched entries", e);
            }
        }
        collector.entries.clear();
    }
}
