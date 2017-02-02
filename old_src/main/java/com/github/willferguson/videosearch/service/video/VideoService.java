package com.github.willferguson.videosearch.service.video;

import com.github.willferguson.videosearch.model.Frame;
import com.github.willferguson.videosearch.model.Status;
import com.github.willferguson.videosearch.model.Video;
import rx.Observable;
import rx.Single;

import java.io.InputStream;
import java.util.Set;

/**
 * Created by will on 02/11/2016.
 */
public interface VideoService {

    void handleUpload(String videoUuid, String filename, InputStream videoFile, Set<String> analysisTypes);

    Single<Status> checkVideoStatus(String videoId);

    Observable<Frame> loadFrames(String videoId, int pageSize, int pageNumber);

    Single<Frame> loadFrame(String videoId, String frameId);

    Single<Video> get(String videoId);
}
