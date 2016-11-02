package com.github.willferguson.videosearch.model;

import org.springframework.data.elasticsearch.annotations.Document;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Simple frame with timestamp
 * Created by will on 27/09/2016.
 */
@Document(indexName = "video", type = "frame")
public class Frame {

    private final String videoId;
    private final String frameId;
    private final String timestamp;
    private InputStream frameData;

    //TODO To begin with we'll just use a list of strings, but we'll need something more expressive
    private List<String> metadata;

    public Frame(String videoId, String frameId, String timestamp, InputStream frameData) {
        this.videoId = videoId;
        this.frameId = frameId;
        this.timestamp = timestamp;
        this.frameData = frameData;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getFrameId() {
        return frameId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public InputStream getFrameData() {
        return frameData;
    }

    public void setFrameData(InputStream frameData) {
        this.frameData = frameData;
    }

    public List<String> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<String> metadata) {
        this.metadata = metadata;
    }
}
