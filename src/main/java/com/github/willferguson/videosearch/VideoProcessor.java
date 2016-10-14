package com.github.willferguson.videosearch;

import com.github.willferguson.videosearch.service.analysis.ImageAnalysisAggregator;
import com.github.willferguson.videosearch.service.search.MetadataIndexer;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import com.github.willferguson.videosearch.storage.ContentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Coordinates all ingest functions:
 *  Frame Extraction (in tmp space)
 *  Store in content store
 *  Initial indexing of frame metadata for lookup
 *  Image Analysis
 *  Indexing of analysis results.
 *
 * Created by will on 27/09/2016.
 */
//TODO Need to track jobs with simple db.
@Component
public class VideoProcessor {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessor.class);

    private final FrameExtractionService frameExtractionService;
    private final ContentStore contentStore;
    private final ImageAnalysisAggregator imageAnalysisAggregator;
    private final MetadataIndexer metadataIndexer;

    @Autowired
    public VideoProcessor(
            FrameExtractionService frameExtractionService,
            ContentStore contentStore,
            ImageAnalysisAggregator imageAnalysisAggregator,
            MetadataIndexer metadataIndexer) {

        this.frameExtractionService = frameExtractionService;
        this.contentStore = contentStore;
        this.imageAnalysisAggregator = imageAnalysisAggregator;
        this.metadataIndexer = metadataIndexer;
    }
}
