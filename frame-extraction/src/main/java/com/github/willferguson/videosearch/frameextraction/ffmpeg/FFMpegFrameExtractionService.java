package com.github.willferguson.videosearch.frameextraction.ffmpeg;

import com.github.willferguson.videosearch.common.io.ObservableStreamGobbler;
import com.github.willferguson.videosearch.frameextraction.FrameExtractionService;
import com.github.willferguson.videosearch.frameextraction.model.Frame;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Uses ffmpeg to extract key frames from the given video.
 *
 * ffmpeg outputs each key frame and a single stdout text file with lines containing the timestamp
 * The we have the lovely job of consuming this file and matching line number with frame.
 *
 * Created by will on 25/09/2016.
 */

public class FFMpegFrameExtractionService implements FrameExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(FFMpegFrameExtractionService.class);

    //TODO Fix this. For some reason the parsing of args doesn't work unless we pass through a shell script.
    private static final String FFMPEG_CMD = "/Users/will/dev/video-search/spit.sh";

    //Args for - select I frames only, name as img_%d.jpg, debug level output (which contains our timestamps per line)
    private static final List<String> FFMPEG_ARGS = Arrays.asList(
            "-vf", "select=eq(pict_type\\,I)",
            "-an", "-vsync", "0", "img_%d.jpeg", "-loglevel debug");

    //Just so we know how to find it in the unique folder.
    private static final String VIDEO_FILE_NAME = "video";

    private static String contentType = "image/jpeg";
    private Path workingDirectory;


    /**
     * @param workingDirectory Where ffmpeg runs.
     */
    public FFMpegFrameExtractionService(Path workingDirectory) {
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
     * with frame meta (timestamp, position, type).
     *
     * @param videoUuid A uuid for the video. This needs to be unique to prevent clashing.
     * @param videoFile The video file to handleUpload.
     * @return
     */
    public Observable<Frame> extractFrames(String videoUuid, InputStream videoFile) {
        logger.debug("Starting frame extraction for video {}", videoUuid);
        //Execute the handleUpload
        return runProcess(videoUuid, videoFile)//then when that's completed, consume the output file
                .andThen(loadFrameData(videoUuid));

    }

    private Completable runProcess(String videoUuid, InputStream videoStream) {

        return Completable.fromAction(() -> {
            try {
                //Create output folder for this job
                Path videoOutputDirectory = createVideoOutputDirectory(videoUuid);

                //Write the input video down. We have to do this as some video sources (mov, mp4)
                //don't like streaming in via stdIn
                Path videoFile = videoOutputDirectory.resolve(VIDEO_FILE_NAME);
                FileOutputStream fileOutputStream = new FileOutputStream(videoFile.toFile());
                IOUtils.copy(videoStream, fileOutputStream);

                //Setup the process execution
                ProcessBuilder builder = new ProcessBuilder();
                //Work in the directory for this video
                builder.directory(videoOutputDirectory.toFile());
                //TODO - Should we consume the error stream separately (this is easiest for now)
                builder.redirectErrorStream(true);

                //Denote a file for ffmpeg stdout (the log data containing our timestamps)
                Path stdOutFile = loadStdOutFile(videoUuid);
                builder.redirectOutput(stdOutFile.toFile());

                List<String> commandAndArgs = new ArrayList<>();
                commandAndArgs.add(FFMPEG_CMD);
                commandAndArgs.add("-i");
                commandAndArgs.add(pathToVideoFile(videoUuid));
                commandAndArgs.addAll(FFMPEG_ARGS);
                builder.command(commandAndArgs);

                logger.info("Executing command {}", commandAndArgs.toString());
                Process process = builder.start();

                logger.info("Waiting for handleUpload to exit");
                int exitCode = process.waitFor();
                logger.info("Process Exited with {}", exitCode);
                //TODO throw and exception if the exitcode isn't 0
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Consume the stdOut file, and return Frame objects with timestamps
    private Observable<Frame> loadFrameData(String videoId) {
        try {
            logger.debug("Reading frame information for video {}", videoId);
            //Consume the file and emit each line
            return ObservableStreamGobbler.gobbleByLine(new FileInputStream(loadStdOutFile(videoId).toFile()))
                    //these are the lines containing the timestamp (the debug info contains loads of other crap)
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
                            (time, file) -> {

                                String[] split = time.split(":");
                                String timestamp = split[1];

                                String filename = file.getName();
                                String frameId = extractFrameId(filename);

                                logger.info("Extracting frame {} for video {} with timestamp {}", frameId, videoId, timestamp);
                                try {
                                    return new Frame(frameId, Double.parseDouble(timestamp), contentType, new FileInputStream(file));
                                } catch (IOException e) {
                                    //TODO Do we want to die if we can't load a particular frame?
                                    throw new RuntimeException(e);
                                }
                            })
                    //Drop the frames where we don't know the timestamp.
                    .filter(frame -> frame.getTimestamp() != -1);
        } catch (FileNotFoundException e) {
            logger.error("Video doesn't exists at {}", loadStdOutFile(videoId).toAbsolutePath());
            return Observable.error(new RuntimeException("Could not find video"));
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
            } else return stdOutFile;
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
