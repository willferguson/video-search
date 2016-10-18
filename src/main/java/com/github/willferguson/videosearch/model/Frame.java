package com.github.willferguson.videosearch.model;

import java.io.InputStream;

/**
 * Simple frame with timestamp
 * Created by will on 27/09/2016.
 */
public class Frame {

    private final String videoId;
    private final String frameId;
    private final String timestamp;
    private InputStream frameData;

    public Frame(String videoId, String frameId, String timestamp) {
        this.videoId = videoId;
        this.frameId = frameId;
        this.timestamp = timestamp;
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
}
