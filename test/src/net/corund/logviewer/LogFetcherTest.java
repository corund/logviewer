package net.corund.logviewer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

public class LogFetcherTest {
    @Test
    public void testLogEntries() throws Exception {
        LogFetcher fetcher = new LogFetcher() {
            protected HttpClient buildClient() {
                HttpResponse response = mock(HttpResponse.class);
                HttpClient client = mock(HttpClient.class);
                try {
                    when(client.execute(isA(HttpGet.class))).thenReturn(response);
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                StatusLine status = mock(StatusLine.class);
                when(status.getStatusCode()).thenReturn(200);
                when(response.getStatusLine()).thenReturn(status);
                
                HttpEntity entity = mock(HttpEntity.class);
                when(response.getEntity()).thenReturn(entity);
                byte[] data = "{\"data\":[], \"result\":200}".getBytes();
                try {
                    when(entity.getContent()).thenReturn(new ByteArrayInputStream(data));
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return client;
            }
        };
        fetcher.setHost("irc.host");
        fetcher.init();
        
        List<LogEntry> entries = fetcher.logEntries(2011, 1, 9);
        
        assertNotNull(entries);
        assertEquals(0, entries.size());
    }
}
