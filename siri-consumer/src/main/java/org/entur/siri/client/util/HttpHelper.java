package org.entur.siri.client.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpHelper {
    private static final int SOCKET_TIMEOUT = 5000;
    private static final int CONN_TIMEOUT = 10000;
    private static final Logger LOG = LoggerFactory.getLogger(HttpHelper.class);

    public static int postData(String url, String xmlData) throws IOException {
        HttpPost httppost = new HttpPost(url);
        if (xmlData != null) {
            httppost.setEntity(new StringEntity(xmlData, ContentType.APPLICATION_XML));
        }

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(CONN_TIMEOUT)
                .setConnectionRequestTimeout(CONN_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT).build();

        HttpClient httpclient = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(config)
                .build();

        HttpResponse response = httpclient.execute(httppost);
        LOG.info("POST request completed with response {}", response.getStatusLine().getStatusCode());

        return response.getStatusLine().getStatusCode();
    }

}
