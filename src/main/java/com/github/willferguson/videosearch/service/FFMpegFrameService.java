package com.github.willferguson.videosearch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by will on 25/09/2016.
 */
public class FFMpegFrameService implements FrameService {

    private static final Logger logger = LoggerFactory.getLogger(FFMpegFrameService.class);
    private Path outputDirectory;

    public FFMpegFrameService(Path outputDirectory) {
        if (!outputDirectory.toFile().canWrite()) {
            throw new RuntimeException("Can not write to output directory");
        }
        this.outputDirectory = outputDirectory;
    }

    @Override
    public String extractFramesWithMetadata(Path videoFile) {
        try {
            ProcessBuilder builder = new ProcessBuilder();

            Process process = Runtime.getRuntime().exec("");

            return "";
        } catch (IOException e) {
            throw new RuntimeException("");
        }
    }
}
