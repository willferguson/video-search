package com.github.willferguson.videosearch.search.model;

import com.github.willferguson.videosearch.common.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple abstraction over an elastic aggregation
 * Created by will on 01/12/2016.
 */
public class FrameAggregation {

    private static final Logger logger = LoggerFactory.getLogger(FrameAggregation.class);

    private String name;
    private String count;

    private Set<FrameAggregation> children = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public Set<FrameAggregation> getChildAggregations() {
        return children;
    }

    public void setChildAggregations(Set<FrameAggregation> subAggregations) {
        this.children = subAggregations;
    }

    public void addChildAggregation(FrameAggregation aggregation) {
        children.add(aggregation);
    }

    public String toJsonString() {
        return Json.toJsonString(this);
    }
}
