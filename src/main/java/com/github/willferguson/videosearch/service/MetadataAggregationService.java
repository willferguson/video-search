package com.github.willferguson.videosearch.service;

import java.io.InputStream;
import java.util.Map;

/*
 * Given a single input image, this dispatches the image to multiple metadata extractors
 * and aggregates the results into a single metadata structure
 * Created by will on 27/09/2016.
 */
public interface MetadataAggregationService {

    /**
     *
     * @param inputStream
     * @return
     */
    public Map<String, Object> generateMetadata(InputStream inputStream);
}
