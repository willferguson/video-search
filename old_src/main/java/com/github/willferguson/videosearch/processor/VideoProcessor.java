package com.github.willferguson.videosearch.processor;

import com.github.willferguson.videosearch.model.*;
import com.github.willferguson.videosearch.persistence.elastic.FrameRepository;
import com.github.willferguson.videosearch.persistence.elastic.VideoRepository;
import com.github.willferguson.videosearch.service.analysis.ImageAnalysisAggregator;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import com.github.willferguson.videosearch.storage.ContentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * TODO Crappy name.
 *
 * This class coordinates all the tasks for video frame extraction and analysis.
 * Going to end up being one long Rx Chain which is hard manage
 * TODO - later we might want to move to Reactor (or similar) event sending to break up into more manageable chunks.
 *
 * Created by will on 16/11/2016.
 */
@Component
public class VideoProcessor {

    private static final Logger logger = LoggerFactory.getLogger(VideoProcessor.class);
    private Executor frameIOExecutor;
    private static final int NUMBER_OF_FRAME_IO_THREADS = 4;
    private final FrameExtractionService frameExtractionService;
    private final ImageAnalysisAggregator imageAnalysisAggregator;
    private final ContentStore contentStore;
    private final VideoRepository videoRepository;
    private final FrameRepository frameRepository;

    public VideoProcessor(
            FrameExtractionService frameExtractionService,
            ImageAnalysisAggregator imageAnalysisAggregator,
            ContentStore contentStore,
            VideoRepository videoRepository,
            FrameRepository frameRepository) {

        this.frameExtractionService = frameExtractionService;
        this.imageAnalysisAggregator = imageAnalysisAggregator;
        this.contentStore = contentStore;
        this.videoRepository = videoRepository;
        this.frameRepository = frameRepository;
        this.frameIOExecutor = Executors.newFixedThreadPool(NUMBER_OF_FRAME_IO_THREADS);
    }

    //Processes each frame - sending for analysis, and indexing into elastic.
    private Observable<Frame> processFrame(Frame frame, Set<String> analysisTypes) {
        //Store the frame, index into elastic, and refresh the input stream.
        return storeAndIndexFrame(frame)
                .flatMap(_frame -> {
                    return imageAnalysisAggregator.generateMetadata(
                            _frame.getFrameData(), _frame.getContentType(), _frame.getContentLength(), analysisTypes)
                            .map(metadata -> addMetadataToFrame(frame, metadata))
                            .doOnSuccess(frameRepository::save)
                            .toObservable();
                });
    }

    /**
     * Download Video
     * Update video status to "EXTRACTING_FRAMES"
     * Pass video to frame extraction service
     * Store and Index each Frame.
     * Send frame for analysis.
     * Update elastic with new metadata
     * Update video status to "FRA
     *
     *
     * @param videoUuid
     * @param analysisTypes
     */
    public void process(String videoUuid, Set<String> analysisTypes) {
        //Pull the video down
        contentStore.retrieveVideo(videoUuid)
                //Update elastic with new status
                .doOnSuccess(inputStream -> {
                    logger.debug("Video retrieved from storage for video {}", videoUuid);
                    updateVideoStatus(videoUuid, Status.EXTRACTING_FRAMES);
                })
                //Begin extraction handleUpload
                .flatMapObservable(inputStream -> {
                    return frameExtractionService.extractFrames(videoUuid, inputStream)
                            //When the frame extraction  has completed, update video status
                            .doOnCompleted(() -> {
                                logger.debug("Frame extraction complete for video {}", videoUuid);
                                updateVideoStatus(videoUuid, Status.FRAMES_EXTRACTED);
                            })
                            .flatMap(frame -> {
                                //This adds parallelism so we can process each frame on the executor
                                return Observable.just(frame)
                                        .observeOn(Schedulers.from(frameIOExecutor))
                                        .flatMap(_frame -> processFrame(_frame, analysisTypes));
                            })
                            .doOnCompleted(() -> updateVideoStatus(videoUuid, Status.FRAMES_ANALYSED));

                })
                .subscribeOn(Schedulers.io())
                .subscribe(
                        frame -> {
                            logger.debug("Analysis and indexing complete for frame {}", frame);
                        },
                        error -> {
                            logger.error("Error processing video", error);
                            updateVideoStatus(videoUuid, Status.FAILED);
                        },
                        () -> {
                            updateVideoStatus(videoUuid, Status.COMPLETE);
                            logger.info("Frame extraction and analysis for video {} complete", videoUuid);
                        });

    }

    //TODO - I'm being lazy - we should refactor all useage of Map<...> out.
    private Frame addMetadataToFrame(Frame frame, Map<String, Set<FrameAttribute>> metadata) {
        Set<AttributeGroup> attributeGroups =
            metadata
                .entrySet()
                .stream()
                .map(entry -> {
                    AttributeGroup group = new AttributeGroup();
                    group.setName(entry.getKey());
                    group.setAttributes(entry.getValue());
                    return group;
                })
                .collect(Collectors.toSet());

        frame.setMetadata(attributeGroups);
        return frame;
    }

    //TODO Better exception handling. Maybe just push through a save rather than get first?
    //TODO Synchronous and blocking
    private void updateVideoStatus(String videoUuid, Status status) {
        logger.debug("Updating video index for {} with {}", videoUuid, status);
        Video video = videoRepository.findOne(videoUuid);
        if (video == null) {
            throw new RuntimeException("No video at uuid " + videoUuid);
        }
        video.setStatus(status);
        videoRepository.save(video);
    }

    //Stores the frame in the content store and indexes in elastic.
    //Also refreshes the input stream, as it's already been read.
    private Observable<Frame> storeAndIndexFrame(Frame frame) {
        logger.debug("Storing frame {} for video {}", frame.getFrameId(), frame.getVideoId());
        return contentStore.storeFrame(frame.getVideoId(), frame.getFrameId(), frame.getFrameData())
                .doOnCompleted(() -> {
                    logger.debug("Indexing frame {} for video {}", frame.getFrameId(), frame.getVideoId());
                    frameRepository.save(frame);
                    logger.debug("Reloading inputstream for frame {} [{}]", frame.getFrameId(), frame.getVideoId());
                })
                //This reloads the input stream on the Frame, as the previous one has been read already and we can't be sure reset is support.
                .andThen(contentStore.retrieveFrame(frame.getVideoId(), frame.getFrameId()))
                .map(inputStream -> {
                    frame.setFrameData(inputStream);
                    return frame;
                }).toObservable();
    }
}
