package com.creditsuisse.loganalyzer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class BufferedRandomAccessFileReaderTest {

static File dataFile;
    
    @BeforeClass
    public static void setup() throws IOException {
        dataFile = File.createTempFile("loganalyzer-e2e", "json");
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < 10; ++i) {
            strBuilder.append(i);
        }
        FileUtils.writeStringToFile(dataFile, strBuilder.toString(), StandardCharsets.UTF_8.name(), true);
        FileUtils.forceDeleteOnExit(dataFile);
    }
    
    @Test
    public void testBufferedRandomAccessFileReader_seek() throws Exception {
        IPartialFileReader reader = 
            new BufferedRandomAccessFileReader(new RandomAccessFile(dataFile, "r"));
        
        reader.seek(9);
        Assert.assertEquals(9L, reader.getFilePointer());
    }
    
    @Test
    public void testBufferedRandomAccessFileReader_readUntilChar() throws Exception {
        IPartialFileReader reader = 
            new BufferedRandomAccessFileReader(new RandomAccessFile(dataFile, "r"));
        
        long maxIndex = dataFile.length();
        String result = reader.readUntilChar('4', maxIndex);
        Assert.assertEquals("01234", result);
        
        result = reader.readUntilChar('9', maxIndex);
        Assert.assertEquals("56789", result);
        
        result = reader.readUntilChar('x', maxIndex);
        Assert.assertNull(result);
    }
    
    @Test
    public void testBufferedRandomAccessFileReader_skipUntilChar() throws Exception {
        IPartialFileReader reader = 
            new BufferedRandomAccessFileReader(new RandomAccessFile(dataFile, "r"));
        
        boolean skipResult = reader.skipUntilChar('2', dataFile.length());
        Assert.assertTrue(skipResult);
        
        String result = reader.readUntilChar('4', dataFile.length());
        Assert.assertEquals("34", result);
        
        skipResult = reader.skipUntilChar('7', dataFile.length());
        Assert.assertTrue(skipResult);
        
        result = reader.readUntilChar('9', dataFile.length());
        Assert.assertEquals("89", result);
        
        skipResult = reader.skipUntilChar('x', dataFile.length());
        Assert.assertFalse(skipResult);
    }
    
    @Test
    public void testBufferedRandomAccessFileReader_skip() throws Exception {
        IPartialFileReader reader = 
            new BufferedRandomAccessFileReader(new RandomAccessFile(dataFile, "r"));
        
        long maxIndex = dataFile.length();
        reader.skip(5);
        String result = reader.readUntilChar('9', maxIndex);
        Assert.assertEquals("56789", result);

    }
}
