package com.github.willferguson.videosearch.service;


import java.nio.file.Path;

/**
 * Created by will on 25/09/2016.
 */
public interface FrameService {


    /**
     * Extracts iFrames from the video file at the given path
     * For each extracted frame, an associated metadata file will be created containing the timestamp of the frame.
     * @param videoFile The video file to process.
     * @return A UUID for the job.
     * This can be used later to retrieve information about the job or retrieve individual frames and metadata.
     */
    public String extractFramesWithMetadata(Path videoFile);
}
