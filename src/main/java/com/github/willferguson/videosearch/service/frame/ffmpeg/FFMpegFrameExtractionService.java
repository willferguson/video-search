package com.github.willferguson.videosearch.service.frame.ffmpeg;

import com.github.willferguson.videosearch.model.Frame;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by will on 25/09/2016.
 */
public class FFMpegFrameExtractionService implements FrameExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(FFMpegFrameExtractionService.class);

    private static final String FFMPEG_CMD = "ffmpeg";
    private static final List<String> FFMPEG_ARGS = Arrays.asList(
            "-i", "$input_video", "-vf", "\"select=eq(pict_type\\,I)\"",
            "-an", "-vsync", "0", "img_%d.jpeg", "-loglevel debug");

    private Path outputDirectory;


    public FFMpegFrameExtractionService(Path outputDirectory) {
//        if (!outputDirectory.toFile().canWrite()) {
//            throw new RuntimeException("Can not write to output directory");
//        }
        this.outputDirectory = outputDirectory;
    }

    //TODO If we have corrupt metadata it's probably best to delete the frame as opposed to exit.
    @Override
    public Observable<Frame> extractFramesWithTimestamp(Path videoFile) {
        try {
            //Create uuid for this job
            String videoId = UUID.randomUUID().toString();
            //Create output folder for this job
            Path jobOutputDirectory = Files.createDirectory(Paths.get(outputDirectory.toString(), videoId));
            AtomicInteger i = new AtomicInteger(0);
            //Execute the process
            return extractLines(jobOutputDirectory, videoFile)
                    .filter(line -> line.contains("select:1"))
                    //Split about the space and return the one with the timestamp (t:1234)
                    .map(line -> {
                        String[] split = line.split(" ");
                        int timestampPosition = -1;
                        for (int j = 0; j < split.length; j++) {
                            if (split[j].matches("t:[0-9]+")) {
                                timestampPosition = j;
                                break;
                            }
                        }
                        if (timestampPosition == -1) {
                            throw new RuntimeException("Corrupt frame metadata");
                        }
                        return split[timestampPosition];

                    })
                    .map(line -> {
                        String[] split = line.split(":");
                        String timestamp = split[1];
                        return new Frame(videoId, Integer.toString(i.getAndIncrement()), timestamp);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Observable<String> extractLines(Path frameOutputDirectory, Path videoFile) {
        try {
            //Execute the task, resulting in frames, and the stdout / err output file.
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(outputDirectory.toFile());
            builder.redirectErrorStream(true);

            List<String> commandAndArgs = new ArrayList<>();
            commandAndArgs.add(FFMPEG_CMD);
            commandAndArgs.addAll(FFMPEG_ARGS);
            builder.command(commandAndArgs);

            Process process = builder.start();
            InputStream stdOut = process.getInputStream();
            return ObservableStreamGobbler.gobble(stdOut);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
