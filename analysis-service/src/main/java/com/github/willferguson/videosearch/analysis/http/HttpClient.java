package com.github.willferguson.videosearch.analysis.http;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * Really simple http client.
 * Created by will on 20/09/2016.
 */
public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    public static String post(URL url, InputStream inputStream, Map<String, String> headers, long contentLength) {

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();

            HttpPost httpPost = new HttpPost(url.toURI());
            headers
                    .entrySet()
                    .forEach(entry -> {
                        httpPost.setHeader(entry.getKey(), entry.getValue());
                    });
            InputStreamEntity inputStreamEntity = new InputStreamEntity(inputStream, contentLength, ContentType.APPLICATION_OCTET_STREAM);
            httpPost.setEntity(inputStreamEntity);


            return httpClient.execute(httpPost, responseHandler);


        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }


    }

    private static ResponseHandler<String> responseHandler = response -> {
        int statusCode = response.getStatusLine().getStatusCode();
        String responseString = EntityUtils.toString(response.getEntity());
        EntityUtils.consume(response.getEntity());
        if (statusCode > 300) {
            throw new RuntimeException("HTTP Error " + statusCode + " " + response.getStatusLine().getReasonPhrase() + " " + responseString);
        }

        return responseString;
    };
}
