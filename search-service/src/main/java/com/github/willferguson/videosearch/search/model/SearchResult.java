package com.github.willferguson.videosearch.search.model;


import com.github.willferguson.videosearch.common.json.Json;

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
