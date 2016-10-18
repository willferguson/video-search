package test;

import com.github.willferguson.videosearch.model.Frame;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import com.github.willferguson.videosearch.service.frame.ffmpeg.FFMpegFrameExtractionService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by will on 15/10/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes= FFMpegFrameExtractionServiceTest.Config.class)
public class FFMpegFrameExtractionServiceTest {

    private final static String OUTPUT_DIR = "/tmp/frame_extraction";

    @Autowired
    FrameExtractionService frameExtractionService;

    @Autowired
    ResourceLoader resourceLoader;

    @Test
    public void testExtractFrames() throws Exception {

        Resource resource = resourceLoader.getResource("classpath:preview.mp4");
        List<Frame> frames = frameExtractionService.extractFramesWithTimestamp(resource.getFile().toPath())
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
        Assert.assertTrue(Files.exists(outputPath));
        frameExtractionService.cleanOutput(frames.get(0).getVideoId())
                .toObservable().toBlocking().subscribe();

        boolean hasChildren = Files.newDirectoryStream(outputPath)
                .iterator().hasNext();
        Assert.assertTrue(!hasChildren);


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
