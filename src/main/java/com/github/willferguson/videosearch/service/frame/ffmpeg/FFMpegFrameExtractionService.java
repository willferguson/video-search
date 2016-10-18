package com.github.willferguson.videosearch.service.frame.ffmpeg;

import com.github.willferguson.videosearch.model.Frame;
import com.github.willferguson.videosearch.retry.RetryStrategy;
import com.github.willferguson.videosearch.retry.delay.Delay;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import rx.Completable;
import rx.Observable;
import rx.internal.operators.OnSubscribeRedo;
import rx.schedulers.Schedulers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by will on 25/09/2016.
 */

public class FFMpegFrameExtractionService implements FrameExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(FFMpegFrameExtractionService.class);

    private static final String FFMPEG_CMD = "/Users/will/dev/video-search/spit.sh";
    private static final List<String> FFMPEG_ARGS = Arrays.asList(
            "-vf", "select=eq(pict_type\\,I)",
            "-an", "-vsync", "0", "img_%d.jpeg", "-loglevel debug");

    private Path outputDirectory;


    public FFMpegFrameExtractionService(Path outputDirectory) {
//        if (!outputDirectory.toFile().canWrite()) {
//            throw new RuntimeException("Can not write to output directory");
//        }
        this.outputDirectory = outputDirectory;
    }

    @Override
    public Completable cleanOutput(String videoId) {
        return Completable.fromAction(() -> {
            Path videoFolder = Paths.get(outputDirectory.toString(), videoId);
            try {
                Files.walkFileTree(videoFolder, new DeletedFileVisitor());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Extracts frame date / info, and returns a stream of {@link Frame} objects
     * describing the frame input stream and timestamp.
     *
     * @param videoFile The video file to process.
     * @return
     */
    @Override
    public Observable<Frame> extractFramesWithTimestamp(Path videoFile) {
        try {
            //Create uuid for this job
            String videoId = UUID.randomUUID().toString();
            //Create output folder for this job
            Path jobOutputDirectory = Files.createDirectory(Paths.get(outputDirectory.toString(), videoId));

            //This keeps track of the frame id. FFMPEG starts at 1.
            AtomicInteger frameId = new AtomicInteger(1);
            //Execute the process
            return extractLines(jobOutputDirectory, videoFile)
                    .filter(line -> line.contains("select:1") && line.contains("pts:"))
                    //Split about the space and return the one with the timestamp (t:1234)
                    .map(line -> {
                        String[] split = line.split(" ");
                        int timestampPosition = -1;
                        for (int j = 0; j < split.length; j++) {
                            if (split[j].matches("t:[0-9]+\\.[0-9]*")) {
                                timestampPosition = j;
                                break;
                            }
                        }
                        //If we don't have a timestamp for this frame (it happens for some reason)
                        //Then add -1. We'll remove this frame later.
                        if (timestampPosition == -1) {
                            return "t:-1";
                        }
                        return split[timestampPosition];

                    })
                    .map(line -> {
                        String[] split = line.split(":");
                        String timestamp = split[1];
                        logger.info("Extracting frame {} for video {} with timestamp {}", frameId, videoId, timestamp);
                        return new Frame(videoId, Integer.toString(frameId.getAndIncrement()), timestamp);
                    })
                    //Drop the frames where we don't know the timestamp.
                    .filter(frame -> !frame.getTimestamp().equals("t:-1"))
                    .flatMap(frame -> {
                        //Load the input stream for this frame, and set on the frame.
                        return loadFrameStream(frame.getVideoId(), frame.getFrameId())
                                .map(inputStream -> {
                                    frame.setFrameData(inputStream);
                                    return frame;
                                });
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Observable<FileInputStream> loadFrameStream(String videoId, String frameId) {

        return Observable.defer(() -> {
            try {
                logger.info("Loading input stream for video {}, frame {}", videoId, frameId);
                return Observable.just(new FileInputStream(findFrame(videoId, frameId).toFile()));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        })
        .retryWhen(
                RetryStrategy
                .builder()
                .delay(Delay.exponential(1, TimeUnit.SECONDS))
                .times(3)
                .when(throwable -> true)
                .build()::run)
        .subscribeOn(Schedulers.io());
    }

    private Path findFrame(String videoId, String frameId) {
        return Paths.get(outputDirectory.toString(), videoId, constructFrameName(frameId));
    }

    private String constructFrameName(String frameId) {
        return "img_" + frameId + ".jpeg";
    }

    private Observable<String> extractLines(Path frameOutputDirectory, Path videoFile) {
        try {
            //Execute the task, resulting in frames, and the stdout / err output file.
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(frameOutputDirectory.toFile());
            builder.redirectErrorStream(true);

            List<String> commandAndArgs = new ArrayList<>();
            commandAndArgs.add(FFMPEG_CMD);
            commandAndArgs.add("-i");
            commandAndArgs.add(videoFile.toString());
            commandAndArgs.addAll(FFMPEG_ARGS);
            builder.command(commandAndArgs);


            logger.info("Executing command {}", commandAndArgs.toString());

            Process process = builder.start();
            InputStream stdOut = process.getInputStream();



            return ObservableStreamGobbler.gobbleByLine(stdOut)
                    .doOnCompleted(() -> {
                        try {
                            process.waitFor();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO This should have some better null checking.
    private class DeletedFileVisitor implements FileVisitor<Path> {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            throw exc;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
