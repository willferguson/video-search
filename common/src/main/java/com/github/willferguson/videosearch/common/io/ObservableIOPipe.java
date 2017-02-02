package com.github.willferguson.videosearch.common.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.observables.StringObservable;

import java.io.*;

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
    //TODO - Test / Play / Implement
    public static Completable rxPipe(InputStream inputStream, OutputStream outputStream) {

        //Create array of pipedoutputstreams, & inptu streams connected.
        //Push input to analyser
        //for each outputstream, write bytes.


        return StringObservable.from(inputStream, 1024 * 1024)

                .map(byteBuffer -> {
                    try {
                        PipedOutputStream pipedOutputStream = new PipedOutputStream();
                        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
                        pipedOutputStream.write(byteBuffer);




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
