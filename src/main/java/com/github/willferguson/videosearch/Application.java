package com.github.willferguson.videosearch;

import com.github.willferguson.videosearch.service.analysis.AnalyzerRegistry;
import com.github.willferguson.videosearch.service.analysis.ImageAnalyser;
import com.github.willferguson.videosearch.service.frame.ffmpeg.FFMpegFrameExtractionService;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import com.github.willferguson.videosearch.storage.ContentStore;
import com.github.willferguson.videosearch.storage.LocalContentStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.nio.file.Paths;

/**
 * Created by will on 30/09/2016.
 */
@SpringBootApplication
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    //TODO Change to load the dir from config server
    @Bean
    public FrameExtractionService frameExtractionService() {
        return new FFMpegFrameExtractionService(Paths.get("Users/will/tmp"));
    }

    //TODO Change to define the content store from config server
    @Bean
    public ContentStore contentStore() {
        return new LocalContentStore();
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(Application.class, args);
        applicationContext.getBeansOfType(ImageAnalyser.class)
                .forEach((name, analyser) -> {
                    AnalyzerRegistry.register(analyser);
                });
    }
}
