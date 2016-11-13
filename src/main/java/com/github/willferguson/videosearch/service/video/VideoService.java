package com.github.willferguson.videosearch.service.video;

import com.github.willferguson.videosearch.model.Status;
import com.github.willferguson.videosearch.model.Video;
import rx.Single;

import java.io.InputStream;

/**
 * Created by will on 02/11/2016.
 */
public interface VideoService {

    void process(String videoUuid, String filename, InputStream videofile);

    Single<Status> checkVideoStatus(String videoId);
}
