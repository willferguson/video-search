package com.github.willferguson.videosearch.analysis.analyzers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by will on 02/10/2016.
 */
public class AnalyzerRegistry {

    private static Map<String, ImageAnalyser> registry = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(AnalyzerRegistry.class);

    public static void register(ImageAnalyser imageAnalyser) {
        registry.put(imageAnalyser.getClass().getName(), imageAnalyser);
    }

    public static Map<String, ImageAnalyser> getRegistry() {
        return registry;
    }

}
