package net.corund.logviewer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class LogFetcher {
    private String host;
    private String baseUrl = "/vd/perl-kr";
    private String username;
    private String password;
    
    private HttpClient client;
    
    /**
     * 초기화 코드. 프로퍼티 값을 설정한 후 호출해야 한다.
     */
    public void init() {
        this.client = buildClient();
    }
    
    public List<LogEntry> logEntries(int year, int month, int day) throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(host);
        sb.append(baseUrl);
        sb.append("/");
        sb.append(String.format("%04d/%02d/%02d", year, month, day));
        
        final HttpGet httpGet = new HttpGet(sb.toString());
        
        HttpResponse response = this.client.execute(httpGet);
        
        int code = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        InputStream is = entity.getContent();

        List<LogEntry> entries = new ArrayList<LogEntry>();
        
        JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
        reader.beginObject();
        String name = reader.nextName();
        reader.beginArray();
        Gson gson = new Gson();
        while (reader.hasNext()) {
            String[] arrs = gson.fromJson(reader, String[].class);
            LogEntry entry = LogEntry.fromArray(arrs);
            entries.add(entry);
        }
        
        reader.endArray();
        name = reader.nextName();
        reader.nextInt();
        reader.endObject();
        
        return entries;
    }
    
    // UnitTest를 할 때 이 메서드를 재정의해서 mockClient를 집어넣는다.
    protected HttpClient buildClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        client.getCredentialsProvider().setCredentials(
                new AuthScope(host, 80),
                new UsernamePasswordCredentials(username, password));
        return client;
    }
    
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
}
