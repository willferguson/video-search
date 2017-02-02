package com.github.willferguson.videosearch.frameextraction;


import com.github.willferguson.videosearch.frameextraction.model.Frame;
import rx.Completable;
import rx.Observable;

import java.io.InputStream;

/**
 * Defines the ability to extract key frames from a given video,
 * load those frames up with a timestamp
 * and later delete the output data when finished.
 * Created by will on 25/09/2016.
 */
public interface FrameExtractionService {

    /**
     * Extracts frames with timestamp for the given video
     *
     * @param videoFile The video file to handleUpload.
     * @return an Observable which emits the {@link Frame}s extracted from the video
     */
    Observable<Frame> extractFrames(String videoId, InputStream videoFile);

    /**
     * Deletes all source files (frames) for the given video.
     * @param videoId The video for which which we want to delete
     * @return A Completable which completes when the output has been deleted.
     */
    Completable cleanOutput(String videoId);

}
