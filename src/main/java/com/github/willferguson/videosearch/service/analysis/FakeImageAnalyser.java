package com.github.willferguson.videosearch.service.analysis;

import com.github.willferguson.videosearch.model.FrameAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

import java.io.InputStream;
import java.util.*;

/**
 * Fake image analyser that returns a dummy set of metadata.
 * Useful for testing or when no internet connection
 * Created by will on 11/11/2016.
 */
public class FakeImageAnalyser implements ImageAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(FakeImageAnalyser.class);

    @Override
    public Single<Map<String, Set<FrameAttribute>>> generateMetadata(InputStream inputStream, String contentType, long contentLength, Set<String> analysisTypes) {
        return Single.fromCallable(() -> {

            Map<String, Set<FrameAttribute>> map = new HashMap<>();
            Set<FrameAttribute> frameAttributes = new HashSet<>();
            frameAttributes.add(new FrameAttribute("test1", 0.999));
            frameAttributes.add(new FrameAttribute("test2", 0.988));
            frameAttributes.add(new FrameAttribute("test3", 0.752));
            map.put("fake", frameAttributes);

            return map;
        });
    }

    @Override
    public Set<String> getAnalysisTypes() {
        return Collections.singleton("fake");
    }
}
