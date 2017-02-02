package com.github.willferguson.videasearch.storage.web;

import com.github.willferguson.videasearch.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Storage REST Controller for storing videos and their associated frames.
 *
 * Created by will on 31/12/2016.
 */
@RestController
@RequestMapping("/storage")
public class StorageRestController implements StorageController<CompletableFuture, MultipartFile> {

    private static final Logger logger = LoggerFactory.getLogger(StorageRestController.class);

    private StorageService storageService;

    @Autowired
    public StorageRestController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Uploads a video.
     * TODO - Consider this being an idempotent PUT at a uuid.
     * @param file
     * @return
     */
    @Override
    @PostMapping("/video")
    public CompletableFuture<ResponseEntity<String>> uploadVideo(@RequestParam("file") MultipartFile file) {

        CompletableFuture<ResponseEntity<String>> futureResponse = new CompletableFuture<>();
        try {
            String videoUuid = UUID.randomUUID().toString();
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream stream = file.getInputStream();
            logger.debug("Received upload for {} with {}", videoUuid, filename);

            //TODO Consider pushing to a different thread.
            storageService.storeVideo(videoUuid, stream)
                    .subscribe(
                            () -> futureResponse.complete(new ResponseEntity<>(videoUuid, HttpStatus.CREATED)),
                            futureResponse::completeExceptionally);

            return futureResponse;
        } catch (IOException e) {
            futureResponse.completeExceptionally(e);
            return futureResponse;
        }
    }

    /**
     * Gets the video data at the specified uuid.
     * @param videoUuid
     * @return
     */
    @Override
    @GetMapping("/video/{videoUuid}")
    public CompletableFuture<ResponseEntity<?>> getVideo(@PathVariable String videoUuid) {
        CompletableFuture<ResponseEntity<?>> futureResponse = new CompletableFuture<>();
        if (storageService.isExternallyAvailable()) {
            storageService.constructExternalVideoURL(videoUuid)
                    .subscribe(
                            url -> futureResponse.complete(constructRedirectResponse(url)),
                            futureResponse::completeExceptionally
                    );
        }
        else {
            storageService.retrieveVideo(videoUuid)
                    .subscribe(
                            stream -> futureResponse.complete(new ResponseEntity<>(stream, HttpStatus.OK)),
                            futureResponse::completeExceptionally
                    );
        }
        return futureResponse;

    }

    @Override
    @PutMapping("/video/{videoUuid}/frame/{frameId}")
    public CompletableFuture<ResponseEntity<Void>> uploadFrame(@RequestParam("file") MultipartFile file,
                                                               @PathVariable String videoUuid,
                                                               @PathVariable String frameId) {
        CompletableFuture<ResponseEntity<Void>> futureResponse = new CompletableFuture<>();
        try {
            InputStream stream = file.getInputStream();
            storageService.storeFrame(videoUuid, frameId, stream)
                    .subscribe(
                            () -> futureResponse.complete(new ResponseEntity<>(HttpStatus.CREATED)),
                            futureResponse::completeExceptionally);

            return futureResponse;
        }
        catch (IOException e) {
            futureResponse.completeExceptionally(e);
            return futureResponse;
        }
    }

    @Override
    @GetMapping("/video/{videoUuid}/frame/{frameId}")
    public CompletableFuture<ResponseEntity<?>> getFrame(@PathVariable String videoUuid,
                                                                   @PathVariable String frameId) {

        CompletableFuture<ResponseEntity<?>> futureResponse = new CompletableFuture<>();

        //If our storage service holds the binaries externally, then we'll just issue a redirect to there.
        if (storageService.isExternallyAvailable()) {
            storageService.constructExternalFrameURL(videoUuid, frameId)
                    .subscribe(
                            url -> {
                                futureResponse.complete(constructRedirectResponse(url));
                            },
                            futureResponse::completeExceptionally
                    );
        }
        //Otherwise, we hold the binary.
        else {
            storageService.retrieveFrame(videoUuid, frameId)
                    .subscribe(
                            stream -> futureResponse.complete(new ResponseEntity<>(stream, HttpStatus.OK)),
                            futureResponse::completeExceptionally
                    );
        }
        return futureResponse;

    }


    private ResponseEntity<Void> constructRedirectResponse(URL url) {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.LOCATION, Collections.singletonList(url.toExternalForm()));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }


}
