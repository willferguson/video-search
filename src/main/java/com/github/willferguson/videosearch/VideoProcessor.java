package com.github.willferguson.videosearch;

import com.github.willferguson.videosearch.persistence.elastic.VideoRepository;
import com.github.willferguson.videosearch.service.analysis.ImageAnalysisAggregator;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import com.github.willferguson.videosearch.storage.ContentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

/**
 * Coordinates all ingest functions:
 * Frame Extraction (in tmp space)
 * Store in content store
 * Initial indexing of frame metadata for lookup
 * Image Analysis
 * Indexing of analysis results.
 *
 * TODO Each individual function should handle it's own retrying and only propegate the error when it's already retried.
 * TODO If function has filed for some reason, we want to be able to resubmit without duplication of date - IE the process chain needs to be idempotent.
 *
 * Created by will on 27/09/2016.
 */
@Component
public class VideoProcessor {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessor.class);

    private final FrameExtractionService frameExtractionService;
    private final ContentStore contentStore;
    private final ImageAnalysisAggregator imageAnalysisAggregator;
    private final VideoRepository videoRepository;


    @Autowired
    public VideoProcessor(
            FrameExtractionService frameExtractionService,
            ContentStore contentStore,
            ImageAnalysisAggregator imageAnalysisAggregator,
            VideoRepository videoRepository) {

        this.frameExtractionService = frameExtractionService;
        this.contentStore = contentStore;
        this.imageAnalysisAggregator = imageAnalysisAggregator;

        this.videoRepository = videoRepository;
    }


    /**
     * Called to kick off a processing job.
     * Non blocking call. Returns immediately.
     * TODO This really should be event based between subsystems.
     * @param videofile The video to process
     * @return A UUID for this video. To be later used for querying.
     */
    public void process(String videoId, InputStream videofile) {




//        Single.just(UUID.randomUUID().toString())
//                .flatMap(
//                        videoId -> contentStore.storeVideo(videoId, videofile)
//                                .toSingle(() -> videoId))
//                .flatMap()(
//                    Single.fromCallable(
//                            () -> {
//                                Video video = new Video();
//                                video.setUuid(videoId);
//                                video.setStatus(Status.STORED);
//                                return videoRepository.save(video);
//                            }
//                    )
//                )
//                .flatMapObservable(video -> {
//                    return frameExtractionService.extractFrames(video.getUuid(), videofile);
//                })
//
//
//
//
//                //TODO For now we'll use IO for all, but best use our own executor.
//                .subscribeOn(Schedulers.io())
//                .subscribe();
    }
}
