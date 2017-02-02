package com.github.willferguson.videosearch.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.willferguson.videosearch.analysis.http.HttpClient;
import com.github.willferguson.videosearch.analysis.model.FrameAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import rx.Single;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by will on 02/10/2016.
 */
@Component
public class MicrosoftVisionAnalyzer implements ImageAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(MicrosoftVisionAnalyzer.class);

    private String visionUrl;
    private String visionKey;

    public MicrosoftVisionAnalyzer(String visionUrl, String visionKey) {
        this.visionUrl = visionUrl;
        this.visionKey = visionKey;
    }

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Single<Map<String, Set<FrameAttribute>>> generateMetadata(InputStream inputStream, String contentType, long contentLength, Set<String> analysisTypes) {
        return Single.fromCallable(() -> {
            try {
                String stringFeatures = commaSeparate(analysisTypes);
                URL url = new URL(visionUrl + "?visualFeatures=" + stringFeatures);
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/octet-stream");
                headers.put("Ocp-Apim-Subscription-Key", visionKey);
                String response = HttpClient.post(url, inputStream, headers, contentLength);

                JsonNode node = objectMapper.readTree(response);
                return analysisTypes
                        .stream()
                        .map(String::toLowerCase)
                        .map(type -> {
                            JsonNode typeNode = node.get(type);
                            Set<FrameAttribute> frameAttributes = StreamSupport.stream(typeNode.spliterator(), true)
                                .map(jsonNode -> {
                                    String name = jsonNode.get("name").asText();
                                    //Annoyingly it they call it score or confidence depending on tag
                                    JsonNode confidenceNode = jsonNode.get("confidence");
                                    if (confidenceNode == null) {
                                        confidenceNode = jsonNode.get("score");
                                    }
                                    double confidence = confidenceNode.asDouble();
                                    return new FrameAttribute(name, confidence);
                                })
                                .collect(Collectors.toSet());
                            return new AbstractMap.SimpleEntry<>(type, frameAttributes);
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

    private String commaSeparate(Collection<String> str) {
        return str.stream().reduce((s1, s2) -> s1 + "," + s2).orElse("");
    }

    private enum Types {
        Categories, Tags, Description, Faces, ImageType, Color, Adult
    }

}
