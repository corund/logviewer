package net.corund.logviewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class LogFetcher {
    private String host;
    private String baseUrl = "/vd/perl-kr";
    
    private HttpClientApi httpClientApi;
    
    public List<LogEntry> logEntries(int year, int month, int day) throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("http://");
        sb.append(host);
        sb.append(baseUrl);
        sb.append("/");
        sb.append(String.format("%04d/%02d/%02d", year, month, day));
        
        final String uri = sb.toString();
        InputStream is = httpClientApi.getContent(uri);

        List<LogEntry> entries = new ArrayList<LogEntry>();
        
        JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
        reader.beginObject();
        
        @SuppressWarnings("unused")
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
        reader.close();
        
        return entries;
    }
    
    public List<Integer> days(int year, int month) throws Exception {
        return requestForIntegers(Utils.join("http://",
                host, baseUrl, "/",
                String.format("%04d/%02d", year, month)));
    }
    
    public List<Integer> month(int year) throws Exception {
        return requestForIntegers(Utils.join("http://",
                host, baseUrl, "/",
                String.format("%04d", year)));
    }
    
    private List<Integer> requestForIntegers(String uri) throws Exception {
        InputStream is = httpClientApi.getContent(uri);
        return readFromJsonToIntegers(is);
    }
    
    private List<Integer> readFromJsonToIntegers(InputStream is) throws IOException {
        List<Integer> ret = new ArrayList<Integer>();
        JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
        reader.beginObject();
        
        @SuppressWarnings("unused")
        String name = reader.nextName();
        reader.beginArray();
        while (reader.hasNext()) {
            String temp = reader.nextString();
            ret.add(Integer.valueOf(temp));
        }
        reader.endArray();
        name = reader.nextName();
        reader.nextInt();
        reader.endObject();
        
        return ret;
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

    public HttpClientApi getHttpClientApi() {
        return httpClientApi;
    }

    public void setHttpClientApi(HttpClientApi httpClientApi) {
        this.httpClientApi = httpClientApi;
    }
}
