package com.github.willferguson.videosearch.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by will on 28/09/2016.
 */
public class S3ContentStore implements ContentStore {

    private static final Logger logger = LoggerFactory.getLogger(S3ContentStore.class);

    @Override
    public void storeFrame(String videodId, String frameId, InputStream inputStream, String contentType) {

    }

    @Override
    public InputStream retrieveFrame(String videoId, String frameId) {
        return null;
    }

    @Override
    public boolean isRedirectable() {
        return true;
    }

    @Override
    public URL redirectToFrame(String videoId, String frameId) {
        return null;
    }

}
