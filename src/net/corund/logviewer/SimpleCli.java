package net.corund.logviewer;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class SimpleCli {
    public static void main(String[] args) throws Exception {
        if (3 != args.length) {
            System.out.println("Usage: " + SimpleCli.class.getName() + " year month day");
            System.exit(1);
        }
        
        int year = Integer.parseInt(args[0]);
        int month = Integer.parseInt(args[1]);
        int day = Integer.parseInt(args[2]);
        
        SimpleCli cli = new SimpleCli();
        cli.init();
        
        List<LogEntry> entries = cli.entries(year, month, day);
        
        cli.display(entries);
    }
    
    private LogFetcher fetcher;
    
    public void init() {
        Properties props = new Properties();
        InputStream is = getClass().getResourceAsStream("config.properties");
        try {
            if (null != is) {
                props.load(is);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        this.fetcher = new LogFetcher();
        fetcher.setHost(props.getProperty("host"));
        
        CommonsHttpClient hClient = new CommonsHttpClient();
        hClient.setHost(props.getProperty("host"));
        hClient.setUsername(props.getProperty("username"));
        hClient.setPassword(props.getProperty("password"));
        hClient.init();
        
        fetcher.setHttpClientApi(hClient);
    }
    
    public List<LogEntry> entries(int year, int month, int day) throws Exception {
        return this.fetcher.logEntries(year, month, day);
    }
    
    public void display(List<LogEntry> entries) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        System.out.println("| nickname\t| time\t| message");
        System.out.println("+---------------+-------+---------------");
        for (LogEntry entry : entries) {
            System.out.print("| ");
            String nickname = entry.getNickname();
            System.out.print(nickname);
            if (nickname.length() < 6) {
                System.out.print("\t");
            }
            System.out.print("\t| ");
            System.out.print(sdf.format(entry.getDatetime()));
            System.out.print("\t| ");
            String message = entry.getMessage();
            message = StringUtils.replace(message, "\n", "\n\t\t\t| ");
            System.out.println(message);
        }
    }
}
