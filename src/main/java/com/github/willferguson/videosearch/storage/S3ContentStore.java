package com.github.willferguson.videosearch.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Single;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by will on 28/09/2016.
 */
public class S3ContentStore implements ContentStore {

    private static final Logger logger = LoggerFactory.getLogger(S3ContentStore.class);

    @Override
    public Completable storeVideo(String videoId, InputStream inputStream) {
        return null;
    }

    @Override
    public Completable storeFrame(String videoId, String frameId, InputStream inputStream) {
        return null;
    }

    @Override
    public Single<InputStream> retrieveVideo(String videodId) {
        return null;
    }

    @Override
    public Single<InputStream> retrieveFrame(String videoId, String frameId) {
        return null;
    }

    @Override
    public boolean isRedirectable() {
        return true;
    }

    @Override
    public Single<URL> redirectToFrame(String videoId, String frameId) {
        return null;
    }
}
