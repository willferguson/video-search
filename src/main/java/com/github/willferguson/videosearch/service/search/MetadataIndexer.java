package com.github.willferguson.videosearch.service.search;

import java.util.Map;

/**
 * Created by will on 28/09/2016.
 */
public interface MetadataIndexer {

    void index(String videoId, String frameId, Map<String, Object> metadata);
}
