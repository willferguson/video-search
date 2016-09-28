package com.github.willferguson.videosearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates all ingest functions:
 *  Frame Extraction (in tmp space)
 *  Store in content store
 *  Initial indexing for lookup
 *  Image Analysis
 *  Indexing of analysis results.
 *
 * Created by will on 27/09/2016.
 */
//TODO Need to track jobs with simple db.
public class VideoProcessor {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessor.class);
}
