package com.github.willferguson.videosearch.model.search;

import com.github.willferguson.videosearch.model.Frame;
import com.github.willferguson.videosearch.utils.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by will on 02/12/2016.
 */
public class SearchResult {

    private List<Frame> frameHits;
    private FrameAggregation frameAggregation;

    public List<Frame> getFrameHits() {
        return frameHits;
    }

    public void setFrameHits(List<Frame> frameHits) {
        this.frameHits = frameHits;
    }

    public FrameAggregation getFrameAggregation() {
        return frameAggregation;
    }

    public void setFrameAggregation(FrameAggregation frameAggregation) {
        this.frameAggregation = frameAggregation;
    }

    public String toJsonString() {
        return Json.toJsonString(this);
    }

}
