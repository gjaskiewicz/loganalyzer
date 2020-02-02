package com.creditsuisse.loganalyzer.data;

/**
 * Data class representing log with matching beginning and end.
 */
public class LogDuration {
    public String id;
    public String type;
    public String host;
    public long timeStarted;
    public long timeEnded; // TODO: depending on use case this might be redundant
    public long timeDelta;
    public boolean alert;
}
