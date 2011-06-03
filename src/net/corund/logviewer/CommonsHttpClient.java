package net.corund.logviewer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class CommonsHttpClient implements HttpClientApi {
    private String host;
    private String username;
    private String password;
    
    private HttpClient client;
    
    public void init() {
        this.client = buildClient();
    }
    
    protected HttpClient buildClient() {
        DefaultHttpClient c = new DefaultHttpClient();
        c.getCredentialsProvider().setCredentials(
                new AuthScope(host, 80),
                new UsernamePasswordCredentials(username, password));
        
        return c;
    }
    
    @Override
    public InputStream getContent(String uri) throws HttpRequestException {
        final HttpGet httpGet = new HttpGet(uri);
        
        HttpResponse response = null;
        try {
            response = this.client.execute(httpGet);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        int code = response.getStatusLine().getStatusCode();
        if (200 != code) {
            throw new HttpRequestException();
        }
        HttpEntity entity = response.getEntity();
        try {
            InputStream is = entity.getContent();
            return is;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HttpRequestException();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new HttpRequestException();
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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
