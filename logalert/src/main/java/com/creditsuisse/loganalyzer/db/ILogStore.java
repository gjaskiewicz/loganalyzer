package com.creditsuisse.loganalyzer.db;

import java.sql.SQLException;

import com.creditsuisse.loganalyzer.data.LogDuration;

/**
 * Interface for log storage.
 */
public interface ILogStore {

    public void insert(LogDuration log) throws SQLException; 
    // TODO: might be better to have custom exception here in future, intraface is abstract while SQL is implementation-specific

    public void close();
}
