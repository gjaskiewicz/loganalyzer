package com.creditsuisse.loganalyzer.testdata;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.creditsuisse.loganalyzer.data.LogEntry;

public class TestData {

    public static void fillTestLogs(File file) throws IOException {
        for (int i = 0; i < 100; ++i) {
            String jsonEntry = String.format(
                "{\"id\":\"%s\", \"state\":\"%s\", \"timestamp\":%d}", 
                "id" + i, LogEntry.State.STARTED, i);
            FileUtils.writeStringToFile(file, jsonEntry, StandardCharsets.UTF_8.name(), true);
        }
        for (int i = 0; i < 100; ++i) {
            String jsonEntry = String.format(
                "{\"id\":\"%s\", \"state\":\"%s\", \"timestamp\":%d}", 
                "id" + i, LogEntry.State.FINISHED, 220 - i);
            FileUtils.writeStringToFile(file, jsonEntry, StandardCharsets.UTF_8.name(), true);
        }
    }
}
