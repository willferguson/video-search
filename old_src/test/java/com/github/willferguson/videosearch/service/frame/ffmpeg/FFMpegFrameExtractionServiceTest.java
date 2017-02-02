package com.github.willferguson.videosearch.service.frame.ffmpeg;

import com.github.willferguson.videosearch.model.Frame;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Created by will on 15/10/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= FFMpegFrameExtractionServiceTest.Config.class)
public class FFMpegFrameExtractionServiceTest {

    private final static String OUTPUT_DIR = "/tmp/frame_extraction";

    @Autowired
    FrameExtractionService frameExtractionService;

    @Autowired
    ResourceLoader resourceLoader;

    @Test
    public void testExtractFrames() throws Exception {

        Resource resource = resourceLoader.getResource("classpath:preview.mp4");
        FileInputStream fileInputStream = new FileInputStream(resource.getFile());
        List<Frame> frames = frameExtractionService.extractFrames(UUID.randomUUID().toString(), fileInputStream)
                .toList()
                .toBlocking()
                .first();

        for (Frame frame : frames) {
            Assert.assertNotNull(frame.getVideoId());
            Assert.assertNotNull(frame.getFrameId());
            Assert.assertNotNull(frame.getTimestamp());
            Assert.assertNotNull(frame.getFrameData());
        }

        Path outputPath = Paths.get(OUTPUT_DIR);
        Path videoOutputFolder = Paths.get(OUTPUT_DIR, frames.get(0).getVideoId());
        Assert.assertTrue(Files.exists(videoOutputFolder));
        frameExtractionService.cleanOutput(frames.get(0).getVideoId())
                .toObservable().toBlocking().subscribe();


        Assert.assertTrue(Files.notExists(videoOutputFolder));
    }


    @Configuration
    public static class Config {

        @Bean
        FrameExtractionService frameExtractionService() {

            try {
                Path tempDir = Paths.get(OUTPUT_DIR);
                if (!Files.exists(tempDir)) {
                    Files.createDirectory(tempDir);
                }
                return new FFMpegFrameExtractionService(tempDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
