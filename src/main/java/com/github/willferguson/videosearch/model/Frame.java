package com.github.willferguson.videosearch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple frame with timestamp.
 *
 * TODO - Need to be able to handle more structured data on a frame.
 * EG - Face detection - emotions for specific faces, safe search detection, dominant colours etc
 *
 *
 * Created by will on 27/09/2016.
 */
@Document(indexName = "video", type = "frame")
public class Frame {

    @Id
    private final String frameId;
    private final String videoId;
    private final String timestamp;
    private final String contentType;

    @Transient
    @JsonIgnore
    private InputStream frameData;

    //Provides tag analysis of the frame.
    private Map<String, Set<FrameAttribute>> metadata;

    public Frame(String frameId, String videoId, String timestamp, String contentType) {
        this.frameId = frameId;
        this.videoId = videoId;
        this.timestamp = timestamp;
        this.contentType = contentType;
    }

    public Frame(String videoId, String frameId, String timestamp, String contentType, InputStream frameData) {
        this.videoId = videoId;
        this.frameId = frameId;
        this.timestamp = timestamp;
        this.contentType = contentType;
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

    public String getContentType() {
        return contentType;
    }

    public Map<String, Set<FrameAttribute>> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Set<FrameAttribute>> metadata) {
        this.metadata = metadata;
    }
}
