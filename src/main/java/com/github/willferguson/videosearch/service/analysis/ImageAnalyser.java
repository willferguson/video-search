package com.github.willferguson.videosearch.service.analysis;

import com.github.willferguson.videosearch.model.FrameAttribute;
import rx.Observable;
import rx.Single;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Created by will on 02/10/2016.
 */
public interface ImageAnalyser {

    /**
     * Executes analysis on the passed input stream, performing the types specified.
     * Implementers can decide
     *
     * @param inputStream
     * @param contentType
     * @param contentLength
     * @param analysisTypes
     * @return
     */
    Single<Map<String, Set<FrameAttribute>>> generateMetadata(
            InputStream inputStream,
            String contentType,
            long contentLength,
            Set<String> analysisTypes);

    /**
     * Returns the set of "analysis types", jobs which this analyzer can perform.
     *
     * TODO Is String expressive enough? IE - need to handle clashing. What happens if we clash? Can we merge results somehow or signify whether merging is acceptable?
     * @return
     */

    Set<String> getAnalysisTypes();
}
