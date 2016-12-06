package com.github.willferguson.videosearch.web;

import com.github.willferguson.videosearch.model.Frame;
import com.github.willferguson.videosearch.model.Video;
import com.github.willferguson.videosearch.service.video.VideoService;
import com.github.willferguson.videosearch.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rx.functions.Func0;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Handles video ingest and status checking for videos
 * Can return videos and frames by id.
 *
 * Async, but not fully reactive until Spring enable multipart support in 5.0M4
 *
 * Created by will on 02/11/2016.
 */
//TODO - Probably not REST if we're being pedantic
@RestController
@RequestMapping("/video")
public class VideoRestController {

    private static final Logger logger = LoggerFactory.getLogger(VideoRestController.class);
    private VideoService videoService;

    @Autowired
    public VideoRestController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Upload a video for processing.
     * Returns the uuid of the video
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String videoUuid = UUID.randomUUID().toString();
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream stream = file.getInputStream();
            logger.debug("Received upload for {} with {}", videoUuid, filename);



            videoService.handleUpload(videoUuid, filename, stream, createDefaultAnalysisTypes());
            //TODO This should return JSON
            return new ResponseEntity<>(videoUuid, HttpStatus.ACCEPTED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Returns a video
     * @param videoId
     * @return
     */
    @GetMapping(value = "/{videoId}")
    public CompletableFuture<ResponseEntity<Video>> getVideo(@PathVariable String videoId) {
        CompletableFuture<ResponseEntity<Video>> response = new CompletableFuture<>();
        videoService.get(videoId)
                .subscribe(
                        video -> response.complete(new ResponseEntity<>(video, HttpStatus.OK)),
                        error -> response.completeExceptionally(error)
                );
        return response;

    }

    /**
     * Checks the status of a video
     * @param videoId
     * @return
     */
    @GetMapping(value = "/{videoId}/status")
    public CompletableFuture<ResponseEntity<Status>> checkStatus(@PathVariable String videoId) {
        CompletableFuture<ResponseEntity<Status>> response = new CompletableFuture<>();
        videoService.checkVideoStatus(videoId)
                .subscribe(
                        status -> response.complete(new ResponseEntity<>(status, HttpStatus.OK)),
                        error -> response.completeExceptionally(error)
                );
        return response;

    }

    /**
     * Returns all frames for a given video.
     * @param videoId
     * @return
     */
    @GetMapping(value = "/{videoId}/frames")
    public CompletableFuture<ResponseEntity<List<Frame>>> loadFrames(@PathVariable String videoId) {
        CompletableFuture<ResponseEntity<List<Frame>>> response = new CompletableFuture<>();

        videoService.loadFrames(videoId, 100, 0)
                .collect((Func0<ArrayList<Frame>>) ArrayList::new, ArrayList::add)
                .subscribe(
                        next -> {
                            response.complete(new ResponseEntity<>(next, HttpStatus.OK));
                        },
                        response::completeExceptionally
                );
        return response;
    }

    /**
     * Returns a specific frame for a video
     * @return
     */
    @GetMapping(value = "/{videoId}/frames/{frameId}")
    public CompletableFuture<ResponseEntity<Frame>> loadFrame(@PathVariable String videoId, @PathVariable String frameId) {
        CompletableFuture<ResponseEntity<Frame>> response = new CompletableFuture<>();
        videoService.loadFrame(videoId, frameId)
                .subscribe(
                        frame -> {
                            response.complete(new ResponseEntity<>(frame, HttpStatus.OK));
                        },
                        response::completeExceptionally
                );
        return response;
    }


    //For now lets hard code some analysis types
    private Set<String> createDefaultAnalysisTypes() {
        Set<String> types = new HashSet<>();
        //MS Emotion
//        types.add("emotion");
//        //MD Tagging
//        types.add("tags");
        //Google
//        types.add("LABEL_DETECTION");
//        types.add("LOGO_DETECTION");
//        types.add("LANDMARK_DETECTION");
//        //Mock
        types.add("dummy");
        types.add("fake");

        return types;
    }
}
