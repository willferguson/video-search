package com.github.willferguson.videasearch.storage.web;

import org.springframework.web.multipart.MultipartFile;

/**
 * TODO Not sure is this generics is useful. If we later want to create a gRPC controller, is this still suitable?
 * Created by will on 30/12/2016.
 */
public interface StorageController<T, INPUT> {


    T uploadVideo(INPUT file);

    T getVideo(String videoUuid);

    T uploadFrame(INPUT file,
                  String videoUuid,
                  String frameId);

    T getFrame(String videoUuid,
               String frameId);
}
