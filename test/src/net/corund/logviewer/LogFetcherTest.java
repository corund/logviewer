package net.corund.logviewer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Test;

public class LogFetcherTest {
    @Test
    public void testLogEntries() throws Exception {
        LogFetcher fetcher = new LogFetcher();
        fetcher.setHost("irc.host");
        
        HttpClientApi client = mock(HttpClientApi.class);
        
        int t1 = (int) (System.currentTimeMillis() / 1000);
        int t2 = (int) (System.currentTimeMillis() / 1000);
        
        // 예제 데이타(총 2개의 log)
        String raw = "{\"data\":[" +
            "[\"jeen\", \"" + t1 + "\", \"드립\"]," +
            "[\"y0ngbin\", \"" + t2 + "\", \"dis\"]" +
            "], \"result\":200}";
        
        byte[] data = raw.getBytes();
        when(client.getContent("http://irc.host" + fetcher.getBaseUrl() + "/2011/01/09")).
            thenReturn(new ByteArrayInputStream(data));
        fetcher.setHttpClientApi(client);
        
        List<LogEntry> entries = fetcher.logEntries(2011, 1, 9);
        
        assertNotNull(entries);
        assertEquals(2, entries.size());
        
        LogEntry entry = entries.get(0);
        assertEquals("jeen", entry.getNickname());
        assertEquals("드립", entry.getMessage());
        assertEquals(t1, entry.getDatetime().getTime() / 1000);
        
        entry = entries.get(1);
        assertEquals("y0ngbin", entry.getNickname());
        assertEquals("dis", entry.getMessage());
        assertEquals(t2, entry.getDatetime().getTime() / 1000);
    }
}
