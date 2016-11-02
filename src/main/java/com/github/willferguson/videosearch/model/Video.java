package com.github.willferguson.videosearch.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * Represents a video, persisted in elastic.
 *
 * Created by will on 31/10/2016.
 */
@Document(indexName = "video", type = "video")
public class Video {

    private static final Logger logger = LoggerFactory.getLogger(Video.class);

    @Id
    private String uuid;
    private Status status;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
