package com.github.willferguson.videosearch.analysis.analyzers.fake;

import com.github.willferguson.videosearch.analysis.model.FrameAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import rx.Single;

import java.io.InputStream;
import java.util.*;

/**
 * Fake image analyser that returns a dummy set of metadata.
 * Useful for testing or when no internet connection
 * Created by will on 11/11/2016.
 */
@Component
public class DummyAnalyser1 extends AbstractDummyAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(DummyAnalyser1.class);

    @Override
    public Single<Map<String, Set<FrameAttribute>>> generateMetadata(InputStream inputStream, String contentType, long contentLength, Set<String> analysisTypes) {
        return Single.fromCallable(() -> {

            //Discard the input stream
            super.sink(inputStream);
            Map<String, Set<FrameAttribute>> map = new HashMap<>();
            Set<FrameAttribute> frameAttributes = new HashSet<>();
            frameAttributes.add(new FrameAttribute("fake" + randomInt() , randomDouble()));
            frameAttributes.add(new FrameAttribute("fake value " + randomInt(), randomDouble()));
            frameAttributes.add(new FrameAttribute("fake tag " + randomInt(), randomDouble()));
            map.put("fake", frameAttributes);



            return map;
        });
    }

    @Override
    public Set<String> getAnalysisTypes() {
        return Collections.singleton("fake");
    }
}
