package com.github.willferguson.videosearch.service.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by will on 02/10/2016.
 */
@Component
public class MicrosoftEmotionAnalyzer implements ImageAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(MicrosoftEmotionAnalyzer.class);

    @Override
    public Map<String, Object> generateMetadata(InputStream inputStream, String contentType, long contentLength, List<String> analysisTypes) {
        return null;
    }

    @Override
    public List<String> getAnalysisTypes() {
        return null;
    }
}
