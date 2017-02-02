package com.github.willferguson.videosearch.search.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.InputStream;
import java.net.URL;
import java.util.Set;

/**
 * Simple frame with timestamp.
 *
 * TODO - Need to be able to handle more structured data on a frame.
 * EG - Face detection - emotions for specific faces, safe search detection, dominant colours etc
 *
 * Created by will on 27/09/2016.
 */
@Document(indexName = "video", type = "frame")
public class Frame {

    @Id
    private String frameId;
    //We don't want partial video id matching
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String videoId;
    private String timestamp;
    private String contentType;
    private long contentLength;

    private URL url;

    @Transient
    @JsonIgnore
    private InputStream frameData;

    //Provides tag analysis of the frame.
    @Field(type = FieldType.Nested)
    private Set<AttributeGroup> metadata;


    public Frame() {
    }

    public Frame(String videoId, String frameId, String timestamp, String contentType, InputStream frameData, long contentLength) {
        this.videoId = videoId;
        this.frameId = frameId;
        this.timestamp = timestamp;
        this.contentType = contentType;
        this.frameData = frameData;
        this.contentLength = contentLength;
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

    public long getContentLength() {
        return contentLength;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public Set<AttributeGroup> getMetadata() {
        return metadata;
    }

    public void setMetadata(Set<AttributeGroup> metadata) {
        this.metadata = metadata;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

}
