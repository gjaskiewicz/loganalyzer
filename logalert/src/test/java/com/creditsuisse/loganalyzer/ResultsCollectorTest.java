package com.creditsuisse.loganalyzer;

import static org.mockito.Mockito.*;

import org.junit.Assert;
import org.junit.Test;

import com.creditsuisse.loganalyzer.data.LogEntry;
import com.creditsuisse.loganalyzer.db.ILogStore;


public class ResultsCollectorTest {

    @Test 
    public void testCollect_unmatched() throws Exception {
        ILogStore mockDb = mock(ILogStore.class);
        ResultsCollector collector = new ResultsCollector(1, mockDb);
        
        collector.collect(new LogEntry("id-1", LogEntry.State.STARTED, "APP_LOG", "host-1", 0L));
        collector.collect(new LogEntry("id-2", LogEntry.State.STARTED, "APP_LOG", "host-2", 1L));
        collector.collect(new LogEntry("id-3", LogEntry.State.STARTED, "APP_LOG", "host-3", 2L));
        
        verify(mockDb, never()).insert(any());
        Assert.assertEquals(3, collector.getEntriesCount());
    }
    
    @Test 
    public void testCollect_matchedSameCollector() throws Exception {
        ILogStore mockDb = mock(ILogStore.class);
        ResultsCollector collector = new ResultsCollector(1, mockDb);
        
        collector.collect(new LogEntry("id-1", LogEntry.State.STARTED, "APP_LOG", "host-1", 0L));
        collector.collect(new LogEntry("id-1", LogEntry.State.FINISHED, "APP_LOG", "host-1", 1L));
        
        verify(mockDb, times(1)).insert(any()); // TODO: test specific id and time delta
        Assert.assertEquals(0, collector.getEntriesCount());
    }
    
    @Test 
    public void testCollect_matchedOtherCollector() throws Exception {
        ILogStore mockDb1 = mock(ILogStore.class);
        ResultsCollector collector1 = new ResultsCollector(1, mockDb1);
        
        ILogStore mockDb2 = mock(ILogStore.class);
        ResultsCollector collector2 = new ResultsCollector(1, mockDb2);
        
        ILogStore mockDb3 = mock(ILogStore.class);
        ResultsCollector collector3 = new ResultsCollector(1, mockDb3);
        
        collector1.collect(new LogEntry("id-1", LogEntry.State.STARTED, "APP_LOG", "host-1", 0L));
        collector2.collect(new LogEntry("id-1", LogEntry.State.FINISHED, "APP_LOG", "host-1", 1L));
        
        collector3.collect(collector1);
        collector3.collect(collector2);
        
        verify(mockDb1, never()).insert(any()); 
        verify(mockDb2, never()).insert(any()); 
        verify(mockDb3, times(1)).insert(any()); 
        Assert.assertEquals(0, collector1.getEntriesCount());
        Assert.assertEquals(0, collector2.getEntriesCount());
        Assert.assertEquals(0, collector3.getEntriesCount());
    }
}
