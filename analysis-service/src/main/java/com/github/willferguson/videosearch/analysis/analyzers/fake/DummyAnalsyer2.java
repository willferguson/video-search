package com.github.willferguson.videosearch.analysis.analyzers.fake;

import com.github.willferguson.videosearch.analysis.model.ImageAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import rx.Single;

import java.io.InputStream;
import java.util.*;

/**
 * Created by will on 15/11/2016.
 */
@Component
public class DummyAnalsyer2 extends AbstractDummyAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(DummyAnalsyer2.class);

    @Override
    public Single<Map<String, Set<ImageAttribute>>> generateMetadata(InputStream inputStream, String contentType, long contentLength, Set<String> analysisTypes) {
        return Single.fromCallable(() -> {
            //Discard the input stream
            super.sink(inputStream);
            Map<String, Set<ImageAttribute>> map = new HashMap<>();
            Set<ImageAttribute> imageAttributes = new HashSet<>();
            imageAttributes.add(new ImageAttribute("dummy tag " + randomInt() , randomDouble()));
            imageAttributes.add(new ImageAttribute("dummy value " + randomInt(), randomDouble()));
            imageAttributes.add(new ImageAttribute("dummy type " + randomInt(), randomDouble()));
            map.put("dummy", imageAttributes);



            return map;
        });
    }

    @Override
    public Set<String> getAnalysisTypes() {
        return Collections.singleton("dummy");
    }
}
