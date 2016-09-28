package com.github.willferguson.videosearch.storage;

import java.io.InputStream;
import java.net.URL;

/**
 * Abstration over a storage medium.
 * Created by will on 28/09/2016.
 */
public interface ContentStore {

    void storeFrame(String videodId, String frameId, InputStream inputStream, String contentType);

    InputStream retrieveFrame(String videoId, String frameId);

    boolean isRedirectable();

    URL redirectToFrame(String videoId, String frameId);


}
