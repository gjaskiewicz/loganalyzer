package com.creditsuisse.loganalyzer;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.Assert;

import com.creditsuisse.loganalyzer.db.ILogStore;
import com.creditsuisse.loganalyzer.util.IPartialFileReader;

public class AnalyzeTaskTest {

    @Test 
    public void testProcessFile_allOk() throws Exception {
        ILogStore mockDb = mock(ILogStore.class);
        ResultsCollector globalCollector = new ResultsCollector(4L, mockDb);
        ResultsCollector localCollector = new ResultsCollector(4L, mockDb);
        AnalyzeTask task = new AnalyzeTask(1, 1, 4L, null, globalCollector);
        
        IPartialFileReader braf = mock(IPartialFileReader.class);
        when(braf.skipUntilChar(eq('{'), anyLong()))
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false);
        
        when(braf.readUntilChar(eq('}'), anyLong()))
            .thenReturn("{\"id\":\"id1\", \"state\":\"STARTED\", \"timestamp\":1}")
            .thenReturn("{\"id\":\"id1\", \"state\":\"FINISHED\", \"timestamp\":2}")
            .thenReturn(null);
        
        task.processFile(braf, localCollector, 100, 0, 100);
        
        verify(mockDb).insert(any());
    }
    
    @Test 
    public void testProcessFile_parseError() throws Exception {
        ILogStore mockDb = mock(ILogStore.class);
        ResultsCollector globalCollector = new ResultsCollector(4L, mockDb);
        ResultsCollector localCollector = new ResultsCollector(4L, mockDb);
        AnalyzeTask task = new AnalyzeTask(1, 1, 4L, null, globalCollector);
        
        IPartialFileReader braf = mock(IPartialFileReader.class);
        when(braf.skipUntilChar(eq('{'), anyLong()))
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false);
        
        when(braf.readUntilChar(eq('}'), anyLong()))
            .thenReturn("{\"id\":\"id1\", \"state\":\"HANGING_THERE\", \"timestamp\":1}")
            .thenReturn("{\"id\":\"id1\", \"state\":\"FINISHED\", \"timestamp\":2}")
            .thenReturn(null);
        
        task.processFile(braf, localCollector, 100, 0, 100);
        
        verify(mockDb, never()).insert(any());
        Assert.assertEquals(1, localCollector.getEntriesCount());
    }
    
    @Test 
    public void testProcessFile_missingFields() throws Exception {
        ILogStore mockDb = mock(ILogStore.class);
        ResultsCollector globalCollector = new ResultsCollector(4L, mockDb);
        ResultsCollector localCollector = new ResultsCollector(4L, mockDb);
        AnalyzeTask task = new AnalyzeTask(1, 1, 4L, null, globalCollector);
        
        IPartialFileReader braf = mock(IPartialFileReader.class);
        when(braf.skipUntilChar(eq('{'), anyLong()))
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false);
        
        when(braf.readUntilChar(eq('}'), anyLong()))
            .thenReturn("{ }")
            .thenReturn("{\"id\":\"id1\", \"state\":\"FINISHED\", \"timestamp\":2}")
            .thenReturn(null);
        
        task.processFile(braf, localCollector, 100, 0, 100);
        
        verify(mockDb, never()).insert(any());
        Assert.assertEquals(1, localCollector.getEntriesCount());
    }
}
