package com.creditsuisse.loganalyzer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.creditsuisse.loganalyzer.db.DB;
import com.creditsuisse.loganalyzer.testdata.TestData;

public class AnalyzeRunnerE2eTest {
    private static final String DB_NAME = "testLogsDb-AnalyzeRunnerE2eTest";
    static File dataFile;
    
    @BeforeClass
    public static void setup() throws IOException {
        dataFile = File.createTempFile("loganalyzer-e2e", "json");
        TestData.fillTestLogs(dataFile);

        FileUtils.forceDeleteOnExit(dataFile);
    }
    
    @Test
    public void testAnalyzeRunner_e2eOneSegment() throws Exception {
        DB db = new DB();
        db.init(DB_NAME);
        db.createDb();
        db.deleteRecords();
        
        try {
            AnalyzeRunner runner = new AnalyzeRunner.Builder(dataFile)
                .withThreads(1)
                .withSegments(1)
                .withDbName(DB_NAME)
                .build();
            
            runner.analyzeFile();
            
            Assert.assertEquals(100L, db.calculateRecordsCount());
        } finally {
            db.close();
        }
    }
    
    @Test
    public void testAnalyzeRunner_e2eManySegments() throws Exception {
        DB db = new DB();
        db.init(DB_NAME);
        db.createDb();
        db.deleteRecords();
        
        try {
            AnalyzeRunner runner = new AnalyzeRunner.Builder(dataFile)
                .withThreads(1)
                .withSegments(400)
                .withDbName(DB_NAME)
                .build();
            
            runner.analyzeFile();
            
            Assert.assertEquals(100L, db.calculateRecordsCount());
        } finally {
            db.close();
        }
    }
}
