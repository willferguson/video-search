package com.github.willferguson.videosearch.state;

import com.github.willferguson.videosearch.exceptions.DuplicateJobException;
import com.github.willferguson.videosearch.exceptions.NoSuchVideoException;
import com.github.willferguson.videosearch.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Completable;
import rx.Observable;
import rx.Single;

import java.util.HashMap;
import java.util.Map;

/**
 * Naive implementation to manage frame state
 *
 * Created by will on 29/10/2016.
 */
public class SimpleVideoStateManager implements VideoStateManager {

    private static final Logger logger = LoggerFactory.getLogger(SimpleVideoStateManager.class);

    private Map<String, Status> state = new HashMap<>();

    @Override
    public Completable submitJob(String videoId) {
        return Completable.fromAction(() -> {
            if (state.putIfAbsent(videoId, Status.COMPLETE) != null) {
                throw new DuplicateJobException(videoId + " Already exists");
            }
        });
    }

    @Override
    public Completable completeJob(String videoId) {
        return Completable.fromAction(() -> {
            state.put(videoId, Status.COMPLETE);
        });
    }

    @Override
    public Completable failJob(String videoId) {
        return Completable.fromAction(() -> {
            state.put(videoId, Status.FAILED);
        });
    }

    @Override
    public Single<Status> findStatus(String videoId) {
        return Observable.just(state.get(videoId))
                .switchIfEmpty(Observable.error(new NoSuchVideoException()))
                .toSingle();
    }



}
