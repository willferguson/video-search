package com.github.willferguson.videosearch.service.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.willferguson.videosearch.model.FrameAttribute;
import com.github.willferguson.videosearch.service.analysis.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Single;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by will on 02/10/2016.
 */
@Component
public class MicrosoftEmotionAnalyzer implements ImageAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(MicrosoftEmotionAnalyzer.class);

    private String emotionUrl;
    private String emotionKey;

    public MicrosoftEmotionAnalyzer(String emotionUrl, String emotionKey) {
        this.emotionUrl = emotionUrl;
        this.emotionKey = emotionKey;
    }



    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Single<Map<String, Set<FrameAttribute>>> generateMetadata(InputStream inputStream, String contentType, long contentLength, @Nullable Set<String> analysisTypes) {

            return Single.fromCallable(() -> {
                try {
                    URL url = new URL(emotionUrl);
                    Map<String, String> headers = new HashMap<>();
                    //headers.put("Content-Type", "application/octet-stream");
                    headers.put("Ocp-Apim-Subscription-Key", emotionKey);
                    String response = HttpClient.post(url, inputStream, headers, contentLength);

                    ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(response);
                    ObjectNode scores = (ObjectNode) arrayNode.get(0).get("scores");
                    Set<FrameAttribute> frameAttributes = StreamSupport.stream(Spliterators.spliterator(scores.fields(), scores.size(), Spliterator.DISTINCT), false)
                            .map(entry -> {
                                String name = entry.getKey();
                                double confidence = entry.getValue().asDouble();
                                return new FrameAttribute(name, confidence);
                            })
                            .collect(Collectors.toSet());

                    Map<String, Set<FrameAttribute>> map = new HashMap<>();
                    map.put(Types.EMOTION.toString().toLowerCase(), frameAttributes);
                    return map;

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

    }

    @Override
    public Set<String> getAnalysisTypes() {
        return EnumSet.allOf(Types.class)
                .stream()
                .map(Enum::toString)
                .collect(Collectors.toSet());
    }

    private enum Types {
        EMOTION;
    }
}


