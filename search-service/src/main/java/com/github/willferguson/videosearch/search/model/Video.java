package com.github.willferguson.videosearch.search.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.net.URL;

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
    private String filename;
    private Status status;
    private URL videoURL;

    public Video() {
    }

    public Video(String uuid, String filename, Status status) {
        this.uuid = uuid;
        this.filename = filename;
        this.status = status;
    }

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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public URL getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(URL videoURL) {
        this.videoURL = videoURL;
    }

    @Override
    public String toString() {
        return "Video{" +
                "uuid='" + uuid + '\'' +
                ", filename='" + filename + '\'' +
                ", status=" + status +
                '}';
    }
}
