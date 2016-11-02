package com.github.willferguson.videosearch.storage;

import rx.Completable;
import rx.Single;

import java.io.InputStream;
import java.net.URL;

/**
 * Rx abstraction over a storage medium.
 *
 * Created by will on 28/09/2016.
 */
public interface ContentStore {

    /**
     * Stores the video.
     * It's the callers job to ensure that the passed videoId is universally unique
     * @param videoId A uuid for this video.
     * @param inputStream The input source for this video
     * @return A Completable that will complete when this video has been stored and is available for retreival.
     */
    Completable storeVideo(String videoId, InputStream inputStream);

    /**
     * Stores the frame for this video. The video must have first been stored.
     * @param videoId A uuid for this video.
     * @param frameId The frame id to store
     * @param inputStream The input source for this frame
     * @return A Completable that will complete when this frame has been stored and is available for retreival.
     */
    Completable storeFrame(String videoId, String frameId, InputStream inputStream);

    Single<InputStream> retrieveVideo(String videodId);

    Single<InputStream> retrieveFrame(String videoId, String frameId);

    Single<Boolean> isRedirectable();

    Single<URL> redirectToFrame(String videoId, String frameId);


}
