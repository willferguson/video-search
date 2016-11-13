package com.github.willferguson.videosearch.web;

import com.github.willferguson.videosearch.service.video.VideoService;
import com.github.willferguson.videosearch.service.video.VideoServiceImpl;
import com.github.willferguson.videosearch.model.Status;
import com.google.common.util.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 *
 * Async, but not fully reactive until Spring enable multipart support in 5.0M4
 *
 * Created by will on 02/11/2016.
 */
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
     * Upload a video for processing
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
            videoService.process(videoUuid, filename, stream);
            return new ResponseEntity<>(videoUuid, HttpStatus.ACCEPTED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/status/{videoId}")
    public CompletableFuture<ResponseEntity<Status>> checkStatus(@PathVariable String videoId) {
        CompletableFuture<ResponseEntity<Status>> response = new CompletableFuture<>();
        videoService.checkVideoStatus(videoId)
                .subscribe(
                        status -> response.complete(new ResponseEntity<>(status, HttpStatus.OK)),
                        error -> response.completeExceptionally(error)
                );
        return CompletableFuture.supplyAsync(
                () -> new ResponseEntity<Status>(Status.STORED, HttpStatus.OK));

    }

}
