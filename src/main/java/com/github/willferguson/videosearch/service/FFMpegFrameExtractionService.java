package com.github.willferguson.videosearch.service;

import com.github.willferguson.videosearch.model.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Created by will on 25/09/2016.
 */
public class FFMpegFrameExtractionService implements FrameExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(FFMpegFrameExtractionService.class);
    private Path outputDirectory;

    public FFMpegFrameExtractionService(Path outputDirectory) {
        if (!outputDirectory.toFile().canWrite()) {
            throw new RuntimeException("Can not write to output directory");
        }
        this.outputDirectory = outputDirectory;
    }

    @Override
    public List<Frame> extractFramesWithTimestamp(Path videoFile) {
        try {
            //Create uuid for this job
            String videoId = UUID.randomUUID().toString();
            //Create output folder for this job
            Path jobOutputDirectory = Files.createDirectory(Paths.get(outputDirectory.toString(), videoId));

            //Execute the process
            return extractFrames(jobOutputDirectory, videoFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Frame> extractFrames(Path frameOutputDirectory, Path videoFile) {
        //Execute the task, resulting in frames, and the stdout / err output file.

        //Process the stdout file, creating multiple metadata file.

        //Read over the output directory, creating the Frame objects and return
        return null;
    }
}
