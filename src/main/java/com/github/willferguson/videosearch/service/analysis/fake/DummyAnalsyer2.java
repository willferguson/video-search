package com.github.willferguson.videosearch.service.analysis.fake;

import com.github.willferguson.videosearch.model.FrameAttribute;
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
    public Single<Map<String, Set<FrameAttribute>>> generateMetadata(InputStream inputStream, String contentType, long contentLength, Set<String> analysisTypes) {
        return Single.fromCallable(() -> {
            //Discard the input stream
            super.sink(inputStream);
            Map<String, Set<FrameAttribute>> map = new HashMap<>();
            Set<FrameAttribute> frameAttributes = new HashSet<>();
            frameAttributes.add(new FrameAttribute("dummy tag " + randomInt() , randomDouble()));
            frameAttributes.add(new FrameAttribute("dummy value " + randomInt(), randomDouble()));
            frameAttributes.add(new FrameAttribute("dummy type " + randomInt(), randomDouble()));
            map.put("dummy", frameAttributes);



            return map;
        });
    }

    @Override
    public Set<String> getAnalysisTypes() {
        return Collections.singleton("dummy");
    }
}
