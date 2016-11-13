package com.github.willferguson.videosearch.analysis;

import com.github.willferguson.videosearch.model.FrameAttribute;
import com.github.willferguson.videosearch.service.analysis.ImageAnalyser;
import com.github.willferguson.videosearch.service.analysis.MicrosoftEmotionAnalyzer;
import com.github.willferguson.videosearch.service.analysis.MicrosoftVisionAnalyzer;
import com.netflix.discovery.converters.Auto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Single;

import java.io.InputStream;
import java.util.*;

/**
 * Created by will on 19/09/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= MicrosoftAnalyserTest.Config.class)
public class MicrosoftAnalyserTest {

    @Autowired
    ImageAnalyser vision;
    @Autowired
    ImageAnalyser emotion;

    @Autowired
    ResourceLoader resourceLoader;

    @Test
    public void testVision() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:dog-hat.jpg");
        long contentLength = resource.contentLength();
        InputStream inputStream = resource.getInputStream();
        Set<String> types = new HashSet<>();
        types.add("Categories");
        types.add("Tags");
        Single<Map<String, Set<FrameAttribute>>> response = vision.generateMetadata(inputStream, "image/jpg", contentLength, types);
        Map<String, Set<FrameAttribute>> map = response.toBlocking().value();
        System.out.println(map.toString());

    }

    @Test
    public void testEmotion() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:smile.jpg");
        long contentLength = resource.contentLength();
        InputStream inputStream = resource.getInputStream();
        Single<Map<String, Set<FrameAttribute>>> response = emotion.generateMetadata(inputStream, "image/jpg", contentLength, null);
        Map<String, Set<FrameAttribute>> map = response.toBlocking().value();
        System.out.println(map.toString());
    }

    @Configuration
    @PropertySources({
            @PropertySource("classpath:microsoft.credentials.properties"),
            @PropertySource("classpath:application.properties")
    })
    public static class Config {

        @Autowired
        Environment environment;


        @Value("${microsoft.vision.key}")
        String visionKey;
        @Value("${microsoft.vision.url}")
        String visionURL;
        @Value("${microsoft.emotion.key}")
        String emotionKey;
        @Value("${microsoft.emotion.url}")
        String emotionURL;

        @Bean(name = "vision")
        public ImageAnalyser microsoftVisionService() {
            return new MicrosoftVisionAnalyzer(visionURL, visionKey);
        }

        @Bean(name = "emotion")
        ImageAnalyser microsoftEmotionService() {
            return new MicrosoftEmotionAnalyzer(emotionURL, emotionKey);
        }

        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyConfig() {
            return new PropertySourcesPlaceholderConfigurer();
        }


    }

}