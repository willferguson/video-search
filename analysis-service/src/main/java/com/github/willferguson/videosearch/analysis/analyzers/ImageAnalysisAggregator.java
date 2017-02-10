package com.github.willferguson.videosearch.analysis.analyzers;

import com.amazonaws.util.IOUtils;
import com.github.willferguson.videosearch.analysis.model.ImageAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Single;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Given a single input image, this dispatches the image to multiple metadata extractors
 * and aggregates the results into a single metadata structure
 * TODO - Need to handle tag clashing
 * Created by will on 27/09/2016.
 */
@Component
public class ImageAnalysisAggregator implements ImageAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisAggregator.class);

    @Override
    public Single<Map<String, Set<ImageAttribute>>> generateMetadata(InputStream inputStream, String contentType, long contentLength, Set<String> analysisTypes) {
        //TODO - This is terribly memory intensive - change to something a little more elegant!
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            return Observable.from(loadAnalyzers())
                    .flatMap(imageAnalyser -> {
                        Set<String> validTypes = selectTypesForAnalyzer(analysisTypes, imageAnalyser);
                        if (validTypes.isEmpty()) {
                            return Observable.empty();
                        }
                        logger.debug("Processing metadata for types {}", validTypes);
                        return imageAnalyser.generateMetadata(new ByteArrayInputStream(bytes), contentType, contentLength, validTypes).toObservable();
                    })
                    .collect(() -> (Map<String, Set<ImageAttribute>>)new HashMap<String, Set<ImageAttribute>>(), (stringSetMap, m) -> {
                        logger.debug("Collecting analysis data {}", m);
                        stringSetMap.putAll(m);
                    })
                    .toSingle();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the union of analysis types for all registered image analysers.
     * TODO - Handle type clashing.
     * @return
     */
    @Override
    public Set<String> getAnalysisTypes() {
        return loadAnalyzers().stream()
                .map(ImageAnalyser::getAnalysisTypes)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<ImageAnalyser> loadAnalyzers() {
        return new HashSet<>(AnalyzerRegistry.getRegistry().values());
    }

    /*
     * Returns the types applicable for the analyser.
     * This effectively removes any invalid types.
     */
    private Set<String> selectTypesForAnalyzer(Set<String> analysisTypes, ImageAnalyser imageAnalyser) {
        return analysisTypes
                .stream()
                .filter(requestedType -> imageAnalyser.getAnalysisTypes().contains(requestedType))
                .collect(Collectors.toSet());
    }

}
