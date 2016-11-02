package com.github.willferguson.videosearch.state;

import com.github.willferguson.videosearch.model.Status;
import rx.Completable;
import rx.Single;

/**
 * Created by will on 29/10/2016.
 */
public interface VideoStateManager {
    Completable submitJob(String videoId);

    Completable completeJob(String videoId);

    Completable failJob(String videoId);

    Single<Status> findStatus(String videoId);
}
