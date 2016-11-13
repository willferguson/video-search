package com.github.willferguson.videosearch.analysis;

import com.github.willferguson.videosearch.model.FrameAttribute;
import com.github.willferguson.videosearch.service.analysis.GoogleVisionAnalyzer;
import com.github.willferguson.videosearch.service.analysis.ImageAnalyser;
import com.github.willferguson.videosearch.service.analysis.MicrosoftEmotionAnalyzer;
import com.github.willferguson.videosearch.service.analysis.MicrosoftVisionAnalyzer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Single;

import javax.imageio.IIOException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by will on 12/11/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= GoogleVisionAnalyserTest.Config.class)
public class GoogleVisionAnalyserTest {

    private static final Logger logger = LoggerFactory.getLogger(GoogleVisionAnalyserTest.class);

    @Autowired
    ImageAnalyser vision;
    @Autowired
    ResourceLoader resourceLoader;

    @Test
    public void testVisionAPI() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:dog-hat.jpg");
        long contentLength = resource.contentLength();
        InputStream inputStream = resource.getInputStream();

        Set<String> types = new HashSet<>();
        types.add("LABEL_DETECTION");
        types.add("LANDMARK_DETECTION");

        Single<Map<String, Set<FrameAttribute>>> response = vision.generateMetadata(inputStream, "image/jpg", contentLength, types);
        Map<String, Set<FrameAttribute>> map = response.toBlocking().value();
        System.out.println(map.toString());
    }

    @Configuration
    @PropertySources({
            @PropertySource("classpath:google.properties"),
            @PropertySource("classpath:application.properties")
    })
    public static class Config {



        @Bean(name = "vision")
        public ImageAnalyser visionService(@Value("${google.storage.bucket.name}") String bucketName,
                                           @Value("${google.application.name}") String applicationName,
                                           ResourceLoader resourceLoader) {
            return new GoogleVisionAnalyzer(bucketName, applicationName, resourceLoader);
        }

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyConfig() {
            return new PropertySourcesPlaceholderConfigurer();
        }


    }
}
