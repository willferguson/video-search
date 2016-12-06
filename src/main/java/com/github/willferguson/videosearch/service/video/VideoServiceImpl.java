package com.github.willferguson.videosearch.service.video;

import com.github.willferguson.videosearch.model.Frame;
import com.github.willferguson.videosearch.model.FrameAttribute;
import com.github.willferguson.videosearch.model.Status;
import com.github.willferguson.videosearch.model.Video;
import com.github.willferguson.videosearch.persistence.elastic.FrameRepository;
import com.github.willferguson.videosearch.persistence.elastic.VideoRepository;
import com.github.willferguson.videosearch.processor.VideoProcessor;
import com.github.willferguson.videosearch.service.analysis.ImageAnalysisAggregator;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import com.github.willferguson.videosearch.storage.ContentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Entry point for most tasks from the web tier.
 * Notifies the video processor of a newly uploaded video once it's been stored.
 * Created by will on 27/09/2016.
 */
@Component
public class VideoServiceImpl implements VideoService {

    private static final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);

    private VideoProcessor videoProcessor;
    private final ContentStore contentStore;

    private final VideoRepository videoRepository;
    private FrameRepository frameRepository;

    @Autowired
    public VideoServiceImpl(
            VideoProcessor videoProcessor,
            ContentStore contentStore,
            VideoRepository videoRepository,
            FrameRepository frameRepository) {

        this.videoProcessor = videoProcessor;
        this.contentStore = contentStore;
        this.videoRepository = videoRepository;
        this.frameRepository = frameRepository;

    }


    /**
     * Called to kick off a processing job.
     * Partially blocking = returns once input stream has ben written.
     *
     * Store the video,
     * Once stored add a referene into elastic
     * Send for frame extraction
     * Update status in elastic
     *
     * @param videoUuid
     * @param filename
     * @param videoFile The video to handleUpload
     * @param analysisTypes The analysis to tbe performed on the frames.
     */
    @Override
    public void handleUpload(String videoUuid, String filename, InputStream videoFile, Set<String> analysisTypes) {
        //Store the video
        contentStore.storeVideo(videoUuid, videoFile)
                //Once stored push into elastic as stored
                .doOnCompleted(() -> {
                    videoRepository.save(new Video(videoUuid, filename, Status.STORED));
                    logger.debug("Video {} indexed", videoUuid);
                    videoProcessor.process(videoUuid, analysisTypes);

                })
                .subscribe(() -> logger.debug("Finished handling video upload for {} [{}]", filename, videoUuid),
                        Throwable::printStackTrace);

    }


    @Override
    public Single<Status> checkVideoStatus(String videoId) {
        return Single.fromCallable(() -> {
            return videoRepository.findOne(videoId);
        })
        .map(Video::getStatus);
    }

    @Override
    public Observable<Frame> loadFrames(String videoId, int pageSize, int pageNumber) {
        return Observable.from(frameRepository.findByVideoId(videoId, new PageRequest(pageNumber, pageSize)));
    }


    @Override
    public Single<Frame> loadFrame(String videoId, String frameId) {
        return null;
    }

    @Override
    public Single<Video> get(String videoId) {
        return null;
    }
}
