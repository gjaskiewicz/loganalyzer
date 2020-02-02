package com.creditsuisse.loganalyzer.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.creditsuisse.loganalyzer.data.LogDuration;

/**
 * HSQLDB class.
 */
public class DB implements ILogStore {
    final static Logger logger = LoggerFactory.getLogger(DB.class);
    
    private Connection conn;
    
    public void init() throws SQLException {
        init("logsDb");
    }
    
    public void init(String dbFile) throws SQLException {
        conn = DriverManager.getConnection("jdbc:hsqldb:file:" + dbFile, "sa", "");
    }
    
    public void createDb() throws SQLException, IOException {
        InputStream inputStream = DB.class.getResourceAsStream("initdb.sql");
        String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        
        Statement stmt = conn.createStatement();
        stmt.execute(text);
    }
    
    public void deleteRecords() throws SQLException {
        Statement stmt = conn.createStatement();
        int dropCount = stmt.executeUpdate("DELETE FROM log_entries");
        
        logger.info("Drop database affected {} rows", dropCount);
    }
    
    public void countRecords() throws SQLException {
        long totalRecords = calculateRecordsCount();
        if (totalRecords >= 0) {
            logger.info("Database has {} rows", totalRecords);
        } else {
            logger.warn("Count query had no effect");
        }
    }
    
    public long calculateRecordsCount() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(1) as ct FROM log_entries");
        
        if (rs.next()) {
            return rs.getLong(1);
        } else {
            return -1;
        }
    }
    
    // TODO: consider bulk-insert for better efficiency
    public void insert(LogDuration log) throws SQLException {       
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO log_entries VALUES (?, ?, ?, ?, ?, ?, ?)");
        pstmt.setString(1, log.id);
        pstmt.setString(2, log.type);
        pstmt.setString(3, log.host);
        pstmt.setLong(4, log.timeStarted);
        pstmt.setLong(5, log.timeEnded);
        pstmt.setLong(6, log.timeDelta);
        pstmt.setBoolean(7, log.alert);
        int updateCount = pstmt.executeUpdate();
        
        if (updateCount == 0) {
            logger.warn("Insert for {} returned 0 update count", log.id);
        }
    }
    
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            logger.error("Cannot close connection to DB", e);
        }
    }
}
