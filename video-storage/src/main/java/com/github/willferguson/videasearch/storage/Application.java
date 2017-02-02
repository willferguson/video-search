package com.github.willferguson.videasearch.storage;

import com.github.willferguson.videasearch.storage.service.LocalStorageService;
import com.github.willferguson.videasearch.storage.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Paths;

/**
 * Entry point for the Spring Boot App
 * Created by will on 19/01/2017.
 */
@SpringBootApplication
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    //TODO Change to define the content store location from config server
    @Bean
    public StorageService localStorageService() {
        return new LocalStorageService(Paths.get("/tmp/videostorage"));
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
