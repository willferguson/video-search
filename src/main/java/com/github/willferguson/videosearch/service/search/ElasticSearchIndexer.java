package com.github.willferguson.videosearch.service.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by will on 02/10/2016.
 */
@Component
public class ElasticSearchIndexer implements MetadataIndexer {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchIndexer.class);

    @Override
    public void index(String videoId, String frameId, Map<String, Object> metadata) {

    }
}
