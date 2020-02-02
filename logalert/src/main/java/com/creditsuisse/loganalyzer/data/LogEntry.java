package com.creditsuisse.loganalyzer.data;

/**
 * Data class representing log entry as read from file.
 */
public class LogEntry {
    
    public enum State {
        STARTED, FINISHED
    }
    
    public String id;
    public State state;
    public String type;
    public String host;
    public long timestamp;
    
    public LogEntry() { }

    public LogEntry(String id, State state, String type, String host, long timestamp) {
        super();
        this.id = id;
        this.state = state;
        this.type = type;
        this.host = host;
        this.timestamp = timestamp;
    }
}
