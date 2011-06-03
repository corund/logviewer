package net.corund.logviewer;

import java.util.Date;

public class LogEntry {
    private String nickname;
    private Date datetime;
    private String message;
    
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public Date getDatetime() {
        return datetime;
    }
    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void populate(String[] arrs) {
        nickname = arrs[0];
        datetime = new Date(Long.parseLong(arrs[1]) * 1000);
        message = arrs[2];
    }
    
    public static LogEntry fromArray(String[] arrs) {
        final LogEntry entry = new LogEntry();
        entry.populate(arrs);
        return entry;
    }
}
