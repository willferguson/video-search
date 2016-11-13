package com.github.willferguson.videosearch.storage;

import com.github.willferguson.videosearch.service.frame.utils.ObservableIOPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import rx.Completable;
import rx.Single;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Used when storing on some local filesystem.
 * All files are stored under the rootDirectory passed the constructor.
 *
 * Stores in structures like:
 * rootDirectory/
 *      videoId/
 *          videoFile
 *          frames/
 *              1
*               2
 *              3
 *
 * <p>
 * Created by will on 28/09/2016.
 */
public class LocalContentStore implements ContentStore {

    private static final Logger logger = LoggerFactory.getLogger(LocalContentStore.class);
    private static final String FRAME_DIR_NAME = "frames";
    private static final String DEFAULT_VIDEO_NAME = "video";
    private Path rootDirectory;


    /**
     * The root directory under which all files will be stored.
     * Attempts to create if it doesn't exist.
     *
     * @param rootDirectory
     */
    @Autowired
    public LocalContentStore(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
        if (!Files.exists(rootDirectory)) {
            try {
                Files.createDirectory(rootDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Could not create root directory", e);
            }
        }
    }

    @Override
    public Completable storeVideo(String videoId, InputStream inputStream) {
        return Single.fromCallable(
                () -> {
                    try {
                        Path videoDirectory = getVideoDirectory(videoId);
                        logger.debug("Creating video directory at {}", videoDirectory.toString());
                        Files.createDirectory(videoDirectory);
                        Path frameDirectory = getFrameDirectory(videoId);
                        logger.debug("Creating frame directory at {}", frameDirectory.toString());
                        Files.createDirectory(frameDirectory);
                        Path video = Paths.get(videoDirectory.toString(), DEFAULT_VIDEO_NAME);
                        logger.debug("Creating video file at {}", video.toString());
                        Files.createFile(video);
                        return new FileOutputStream(video.toFile());
                    } catch (IOException e) {
                        throw new RuntimeException("Could not open file for writing", e);
                    }
                })
                .flatMapCompletable(outputStream -> ObservableIOPipe.traditionalPipe(inputStream, outputStream));

    }

    @Override
    public Completable storeFrame(String videoId, String frameId, InputStream inputStream) {
        return Single.fromCallable(
                () -> {
                    try {
                        Path frame = Paths.get(getFrameDirectory(videoId).toString(), frameId);
                        logger.debug("Storing frame {} at {}", frameId, frame.toString());
                        Files.createFile(frame);
                        return new FileOutputStream(frame.toFile());
                    } catch (IOException e) {
                        throw new RuntimeException("Could not open file for writing", e);
                    }
                })
                .flatMapCompletable(outputStream -> ObservableIOPipe.traditionalPipe(inputStream, outputStream));
    }

    @Override
    public Single<InputStream> retrieveVideo(String videoId) {
        return Single.fromCallable(() -> {
            Path video = getVideoDirectory(videoId).resolve(DEFAULT_VIDEO_NAME);
            logger.debug("Retrieving video at {}", video.toString());
            return new FileInputStream(video.toFile());
        });
    }

    @Override
    public Single<InputStream> retrieveFrame(String videoId, String frameId) {
        return Single.fromCallable(() -> {
            Path frame = getFrameDirectory(videoId).resolve(frameId);
            logger.debug("Retrieving frame at {}", frame.toString());
            return new FileInputStream(frame.toFile());
        });
    }

    @Override
    public boolean isRedirectable() {
        return false;
    }

    @Override
    public Single<URL> redirectToFrame(String videoId, String frameId) {
        return Single.error(new UnsupportedOperationException("Files Entities are not redirectable"));
    }

    private Path getVideoDirectory(String videoId) {
        return Paths.get(rootDirectory.toString(), videoId);
    }

    private Path getFrameDirectory(String videoId) {
        return Paths.get(getVideoDirectory(videoId).toString(), FRAME_DIR_NAME);
    }

}
