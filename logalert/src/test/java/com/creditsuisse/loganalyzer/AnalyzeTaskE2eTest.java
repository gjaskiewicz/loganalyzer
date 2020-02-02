package com.creditsuisse.loganalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.junit.BeforeClass;
import org.apache.commons.io.FileUtils;

import com.creditsuisse.loganalyzer.data.LogEntry;
import com.creditsuisse.loganalyzer.db.DB;
import com.creditsuisse.loganalyzer.testdata.TestData;

public class AnalyzeTaskE2eTest {

    private static final String DB_NAME = "testLogsDb-AnalyzeTaskE2eTest";
    static File dataFile;
    
    @BeforeClass
    public static void setup() throws IOException {
        dataFile = File.createTempFile("loganalyzer-e2e", "json");
        TestData.fillTestLogs(dataFile);

        FileUtils.forceDeleteOnExit(dataFile);
    }
    
    @Test 
    public void testAnalyzeTask_e2e() throws Exception {
        DB db1 = new DB();
        db1.init(DB_NAME);
        db1.createDb();
        db1.deleteRecords();
        
        DB db2 = new DB();
        db2.init(DB_NAME);
        
        DB db3 = new DB();
        db3.init(DB_NAME);
        
        try {
            ResultsCollector collector = new ResultsCollector(
                50 /* alertThreshold */,
                db1);
            AnalyzeTask task = new AnalyzeTask(
                0 /* workerNumber */, 
                1 /* totalWorkers */,
                50 /* alertThreshold */,
                dataFile,
                collector);
            task.setDbSupplier(() -> db2);
            task.call();
            
            Assert.assertEquals(100, db3.calculateRecordsCount());
        } finally {
            db1.close();
            db2.close();
            db3.close();
        }
    }
}
