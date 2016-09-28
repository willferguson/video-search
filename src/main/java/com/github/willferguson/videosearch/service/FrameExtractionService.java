package com.github.willferguson.videosearch.service;


import com.github.willferguson.videosearch.model.Frame;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by will on 25/09/2016.
 */
public interface FrameExtractionService {


    /**
     * Extracts iFrames from the video file at the given path
     * For each extracted frame, an associated metadata file will be created containing the timestamp of the frame.
     * @param videoFile The video file to process.
     * @return A list of Frame objects detailing the output of the task
     */
    public List<Frame> extractFramesWithTimestamp(Path videoFile);
}
