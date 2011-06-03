package net.corund.logviewer;

import java.io.InputStream;

public interface HttpClientApi {
    InputStream getContent(String uri) throws HttpRequestException;
}
