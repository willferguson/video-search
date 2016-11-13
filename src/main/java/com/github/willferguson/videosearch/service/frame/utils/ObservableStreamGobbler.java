package com.github.willferguson.videosearch.service.frame.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Scheduler;
import rx.observables.StringObservable;
import rx.schedulers.Schedulers;

import java.io.*;

/**
 * Returns an Observable that emits each line of the input stream

 * Created by will on 13/10/2016.
 */
public class ObservableStreamGobbler {

    private static final Logger logger = LoggerFactory.getLogger(ObservableStreamGobbler.class);

    /**
     * Returns an Observable that emits each line of the input stream as a String.
     * @param inputStream The stream to gobble
     * @param scheduler The thread (scheduler) on which to run.
     * @return
     */
    public static Observable<String> gobble(InputStream inputStream, Scheduler scheduler) {
        return Observable.<String, Reader>using(
                () -> {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    return new BufferedReader(inputStreamReader);
                },
                StringObservable::from,
                reader -> {
                    try {
                        reader.close();
                        inputStream.close();
                    } catch (IOException e) {
                        throw new RuntimeException("Could not close Reader", e);
                    }
                })
                .subscribeOn(scheduler);
    }

    /**
     * As {@link #gobble(InputStream, Scheduler)} but runs on the {@link Schedulers#io()}
     * @param inputStream
     * @return
     */
    public static Observable<String> gobble(InputStream inputStream) {
        return gobble(inputStream, Schedulers.io());
    }

    public static Observable<String> gobbleByLine(InputStream inputStream) {
        return StringObservable.byLine(gobble(inputStream));
    }

    public static Observable<String> gobbleByLine(InputStream inputStream, Scheduler scheduler) {
        return StringObservable.byLine(gobble(inputStream, scheduler));
    }
}
