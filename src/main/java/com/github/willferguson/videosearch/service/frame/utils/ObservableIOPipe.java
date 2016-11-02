package com.github.willferguson.videosearch.service.frame.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.observables.StringObservable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pipes data from input to output.
 * Does not operate on any particular scheduler
 * Created by will on 19/10/2016.
 */
public class ObservableIOPipe {

    private static final Logger logger = LoggerFactory.getLogger(ObservableIOPipe.class);

    public static Completable traditionalPipe(InputStream inputStream, OutputStream outputStream) {
        return Completable.fromAction(() -> {
                    try {
                        int i = -1;
                        byte[] buffer = new byte[1024];
                        while ((i = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, i);
                        }
                    }
                    catch (IOException e) {
                        logger.error("", e);
                        throw new RuntimeException(e);
                    }
                    finally {
                        try {
                            inputStream.close();
                            outputStream.close();
                        } catch (IOException e) {
                            logger.warn("", e);
                        }
                    }

                });
    }

    public static Completable rxPipe(InputStream inputStream, OutputStream outputStream) {
        return StringObservable.from(inputStream, 1024 * 1024)
                .map(byteBuffer -> {
                    try {
                        //logger.info("Writing buffer size {}", byteBuffer.length);
                        outputStream.write(byteBuffer);
                        return outputStream;

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toCompletable();

    }


}
