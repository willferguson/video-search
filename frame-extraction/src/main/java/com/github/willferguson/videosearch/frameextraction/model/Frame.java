package com.github.willferguson.videosearch.frameextraction.model;

import java.io.InputStream;

/**
 * Created by will on 20/01/2017.
 */
public class Frame {

    private final String position;
    private final Double timestamp;
    private final String contentType;
    private final InputStream inputStream;

    public Frame(String position, Double timestamp, String contentType, InputStream inputStream) {
        this.position = position;
        this.timestamp = timestamp;
        this.contentType = contentType;
        this.inputStream = inputStream;
    }

    public String getContentType() {
        return contentType;
    }

    public String getPosition() {
        return position;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public Double getTimestamp() {
        return timestamp;
    }

}
