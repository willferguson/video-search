package com.github.willferguson.videosearch.service.frame.ffmpeg;

import com.github.willferguson.videosearch.exceptions.NoSuchVideoException;
import com.github.willferguson.videosearch.model.Frame;
import com.github.willferguson.videosearch.model.Status;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import com.github.willferguson.videosearch.service.frame.utils.ObservableStreamGobbler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.Single;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * Created by will on 25/09/2016.
 */

public class FFMpegFrameExtractionService implements FrameExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(FFMpegFrameExtractionService.class);

    private static final String FFMPEG_CMD = "/Users/will/dev/video-search/spit.sh";
    private static final List<String> FFMPEG_ARGS = Arrays.asList(
            "-vf", "select=eq(pict_type\\,I)",
            "-an", "-vsync", "0", "img_%d.jpeg", "-loglevel debug");

    private static final String VIDEO_FILE_NAME = "video";

    private static String contentType = "image/jpeg";
    private Path workingDirectory;


    public FFMpegFrameExtractionService(Path workingDirectory) {
//        if (!Files.isWritable(workingDirectory)) {
//            throw new RuntimeException("Can not write to output directory");
//        }
        this.workingDirectory = workingDirectory;
    }

    @Override
    public Completable cleanOutput(String videoId) {
        return Completable.fromAction(() -> {
            Path videoFolder = Paths.get(workingDirectory.toString(), videoId);
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
     * @param videoFile The video file to handleUpload.
     * @return
     */
    public Observable<Frame> extractFrames(String videoId, InputStream videoFile) {
        logger.debug("Starting frame extraction for video {}", videoId);
        //Execute the handleUpload
        return runProcess(videoId, videoFile)//then when that's completed, consume the output file
                .andThen(loadFrames(videoId));

    }

    private Observable<Frame> loadFrames(String videoId) {

        try {
            logger.debug("Reading frame information for video {}", videoId);
            return ObservableStreamGobbler.gobbleByLine(new FileInputStream(loadStdOutFile(videoId).toFile()))
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
                    // Zipping "t:123" with the output frame for that timestamp.
                    .zipWith(
                            loadFiles(videoId),
                            (time, file)  -> {

                                String[] split = time.split(":");
                                String timestamp = split[1];

                                String filename = file.getName();
                                String frameId = extractFrameId(filename);

                                logger.info("Extracting frame {} for video {} with timestamp {}", frameId, videoId, timestamp);
                                try {
                                    return new Frame(videoId, frameId, timestamp, contentType, new FileInputStream(file), file.length());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                    //Drop the frames where we don't know the timestamp.
                    .filter(frame -> !frame.getTimestamp().equals("t:-1"));
        } catch (FileNotFoundException e) {
            return Observable.error(new NoSuchVideoException("No video with this id"));
        }


    }

    private String extractFrameId(String frameName) {
        return frameName.substring(4, frameName.length() - 5);
    }

    private Path createVideoOutputDirectory(String videoId) {
        try {
            Path videoOutputDirectory = workingDirectory.resolve(videoId);
            logger.debug("Creating output directory for video at {}", videoOutputDirectory.toString());
            Files.createDirectory(videoOutputDirectory);
            return videoOutputDirectory;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private Completable runProcess(String videoId, InputStream videoStream) {
        try {
            //Create output folder for this job
            Path videoOutputDirectory = createVideoOutputDirectory(videoId);

            //Write the input video down. We have to do this as some video sources (mov, mp4)
            //don't like streaming in via stdIn
            Path videoFile = videoOutputDirectory.resolve(VIDEO_FILE_NAME);
            FileOutputStream fileOutputStream = new FileOutputStream(videoFile.toFile());
            IOUtils.copy(videoStream, fileOutputStream);

            //Setup the process execution
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(videoOutputDirectory.toFile());
            builder.redirectErrorStream(true);

            Path stdOutFile = loadStdOutFile(videoId);

            builder.redirectOutput(stdOutFile.toFile());
            List<String> commandAndArgs = new ArrayList<>();
            commandAndArgs.add(FFMPEG_CMD);
            commandAndArgs.add("-i");
            commandAndArgs.add(pathToVideoFile(videoId));
            commandAndArgs.addAll(FFMPEG_ARGS);
            builder.command(commandAndArgs);

            logger.info("Executing command {}", commandAndArgs.toString());
            Process process = builder.start();



            return Completable.fromAction(() -> {
                        try {
                            logger.info("Waiting for handleUpload to exit");
                            process.waitFor();
                            logger.info("Process Exited");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String pathToVideoFile(String videoId) {
        return Paths.get(workingDirectory.toString(), videoId, VIDEO_FILE_NAME).toString();
    }

    //Loads the standard out file, creating if it doesn't exist.
    private Path loadStdOutFile(String videoId) {
        Path stdOutFile = Paths.get(workingDirectory.toString(), videoId, "out.txt");
        try {
            if (!stdOutFile.toFile().exists()) {
                return Files.createFile(stdOutFile);
            }
            else return stdOutFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Emits all key frames images sorted to frame order
    private Observable<File> loadFiles(String videoId) {
        return Observable.just(Paths.get(workingDirectory.toString(), videoId))
                .flatMap(videoDirectory -> {
                    File[] frames = videoDirectory
                            .toFile()
                            .listFiles(file -> file.getName().matches("img_[0-9]+\\.jpeg"));
                    if (frames == null) {
                        return Observable.empty();
                    }
                    return Observable.from(frames);
                })
                .sorted((f1, f2) -> {
                    String frameName1 = f1.getName();
                    String frameName2 = f2.getName();
                    Integer frameId1 = Integer.parseInt(extractFrameId(frameName1));
                    Integer frameId2 = Integer.parseInt(extractFrameId(frameName2));
                    return Integer.compare(frameId1, frameId2);
                });
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
