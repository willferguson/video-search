package com.github.willferguson.videosearch.service.analysis;

import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Given a single input image, this dispatches the image to multiple metadata extractors
 * and aggregates the results into a single metadata structure
 * TODO - Need to handle tag clashing
 * Created by will on 27/09/2016.
 */
@Component
public class ImageAnalysisAggregator {

    /**
     * Executes all the requested analysis types on the passed input stream.
     *
     * If a requested analsyis isn't present....
     *
     *
     * @param inputStream
     * @param contentType
     * @param contentLength
     * @param analysisTypes
     * @return
     */
    public Map<String, Object> generateMetadata(
            InputStream inputStream,
            String contentType,
            long contentLength,
            List<String> analysisTypes) {

        return null;
    }


    private Set<ImageAnalyser> loadAnalyzers() {
        return null;
    }

}
