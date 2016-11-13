package com.github.willferguson.videosearch.service.video;

import com.github.willferguson.videosearch.model.Frame;
import com.github.willferguson.videosearch.model.Status;
import com.github.willferguson.videosearch.model.Video;
import com.github.willferguson.videosearch.persistence.elastic.FrameRepository;
import com.github.willferguson.videosearch.persistence.elastic.VideoRepository;
import com.github.willferguson.videosearch.service.analysis.ImageAnalysisAggregator;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import com.github.willferguson.videosearch.storage.ContentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
public class VideoServiceImpl implements VideoService {

    private static final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);

    private final FrameExtractionService frameExtractionService;
    private final ContentStore contentStore;
    private final ImageAnalysisAggregator imageAnalysisAggregator;
    private final VideoRepository videoRepository;
    private FrameRepository frameRepository;
    private Executor frameIOExecutor;
    private static final int NUMBER_OF_FRAME_IO_THREADS = 4;

    @Autowired
    public VideoServiceImpl(
            FrameExtractionService frameExtractionService,
            ContentStore contentStore,
            ImageAnalysisAggregator imageAnalysisAggregator,
            VideoRepository videoRepository,
            FrameRepository frameRepository) {

        this.frameExtractionService = frameExtractionService;
        this.contentStore = contentStore;
        this.imageAnalysisAggregator = imageAnalysisAggregator;

        this.videoRepository = videoRepository;
        this.frameRepository = frameRepository;
        this.frameIOExecutor = Executors.newFixedThreadPool(NUMBER_OF_FRAME_IO_THREADS);
    }


    /**
     * Called to kick off a processing job.
     * Non blocking call. Returns immediately.
     *
     * Store the video,
     * Once stored add a referene into elastic
     * Send for frame extraction
     * Update status in elastic
     *
     * @param videoUuid
     * @param videofile The video to process
     * @return A UUID for this video. To be later used for querying.
     */
    @Override
    public void process(String videoUuid, String filename, InputStream videofile) {
        //Store the video
        contentStore.storeVideo(videoUuid, videofile)
                //Once stored push into elastic as stored
                .doOnCompleted(() -> {
                    videoRepository.save(new Video(videoUuid, filename, Status.STORED));
                    logger.debug("Video {} indexed", videoUuid);

                })
                //Pull the video down
                .andThen(contentStore.retrieveVideo(videoUuid)
                        //Update elastic with new status
                        .doOnSuccess(inputStream -> {
                            videoRepository.save(new Video(videoUuid, filename, Status.EXTRACTING_FRAMES));
                            logger.debug("Video retrieved from storage and index updated");
                        }))
                //Begin extraction process
                .flatMapObservable(inputStream -> frameExtractionService.extractFrames(videoUuid, inputStream)
                        //When the frame extraction process has completed, update video repo
                        .doOnCompleted(() -> {
                            logger.debug("Updating video index for {} with {}", videoUuid, Status.FRAMES_EXTRACTED);
                            videoRepository.save(new Video(videoUuid, filename, Status.FRAMES_EXTRACTED));
                        }))
                //Store each frame
                .flatMap(this::storeAndIndex)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        next -> {},
                        Throwable::printStackTrace,
                        () -> logger.debug("Finished processing video"));

    }

    //Stores the frame in the content store and indexes in elastic.
    //Does all work on the frame io executor in parallel
    private Observable<Frame> storeAndIndex(Frame frame) {
        return Observable.just(frame)
                .subscribeOn(Schedulers.from(frameIOExecutor))
                .flatMap(_frame -> {
                    logger.debug("Storing frame {} for video {}", frame.getFrameId(), frame.getVideoId());
                    return contentStore.storeFrame(frame.getVideoId(), frame.getFrameId(), frame.getFrameData())
                            .doOnCompleted(() -> {
                                logger.debug("Indexing frame {} for video {}", frame.getFrameId(), frame.getVideoId());
                                frameRepository.save(frame);
                            })
                            .toSingle(() -> _frame)
                            .toObservable();
                });

    }

    @Override
    public Single<Status> checkVideoStatus(String videoId) {
        return Single.fromCallable(() -> {
            return videoRepository.findOne(videoId);
        })
        .map(Video::getStatus);
    }
}
