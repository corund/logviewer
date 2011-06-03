package net.corund.logviewer;

import static org.junit.Assert.*;

import org.junit.Test;

public class LogEntryTest {
    @Test
    public void testPopulate() throws Exception {
        final LogEntry entry = new LogEntry();
        
        final String nickname = "keedi";
        final long time = System.currentTimeMillis() / 1000;
        final String message = "message2 message1 http://twitter.com/keedi;";
        
        String[] arrs = new String[] {
                nickname,
                Long.toString(time),
                message
        };
        
        entry.populate(arrs);
        
        assertEquals(nickname, entry.getNickname());
        assertEquals(time, entry.getDatetime().getTime() / 1000);
        assertEquals(message, entry.getMessage());
    }
}
