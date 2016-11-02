package com.github.willferguson.videosearch.storage;

import com.github.willferguson.videosearch.service.frame.utils.ObservableIOPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Component;
import rx.Completable;
import rx.Single;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Used when storing on some local filesystem.
 * All files are stored under the rootDirectory passed the constructor.
 * <p>
 * Created by will on 28/09/2016.
 */
public class LocalContentStore implements ContentStore {

    private static final Logger logger = LoggerFactory.getLogger(LocalContentStore.class);
    private Path rootDirectory;
    private ResourceLoader resourceLoader;


    /**
     * The root directory under which all files will be stored.
     * Attempts to create if it doesn't exist.
     *
     * @param rootDirectory
     */
    @Autowired
    public LocalContentStore(Path rootDirectory, ResourceLoader resourceLoader) {
        this.rootDirectory = rootDirectory;
        this.resourceLoader = resourceLoader;
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
                        WritableResource resource = (WritableResource) resourceLoader.getResource(
                                rootDirectory.toString()).createRelative(videoId);
                        return resource.getOutputStream();
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
                        WritableResource resource = (WritableResource) resourceLoader.getResource(
                                rootDirectory.toString() + File.separator + videoId).createRelative(frameId);
                        return resource.getOutputStream();
                    } catch (IOException e) {
                        throw new RuntimeException("Could not open file for writing", e);
                    }
                })
                .flatMapCompletable(outputStream -> ObservableIOPipe.traditionalPipe(inputStream, outputStream));
    }

    @Override
    public Single<InputStream> retrieveVideo(String videoId) {
        return Single.fromCallable(() -> {
            return resourceLoader.getResource(
                    rootDirectory.toString() + File.separator + videoId).getInputStream();
        });
    }

    @Override
    public Single<InputStream> retrieveFrame(String videoId, String frameId) {
        return Single.fromCallable(() -> {
            return resourceLoader.getResource(
                    rootDirectory.toString() + File.separator + videoId + File.separator + frameId).getInputStream();
        });
    }

    @Override
    public Single<Boolean> isRedirectable() {
        return Single.just(false);
    }

    @Override
    public Single<URL> redirectToFrame(String videoId, String frameId) {
        return Single.error(new UnsupportedOperationException("Files Entities are not redirectable"));
    }

}
