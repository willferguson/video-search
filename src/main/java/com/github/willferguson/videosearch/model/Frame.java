package com.github.willferguson.videosearch.model;

/**
 * Simple frame with timestamp
 * Created by will on 27/09/2016.
 */
public class Frame {

    private final String videoId;
    private final String frameId;
    private final String timestamp;

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
}
