package com.github.willferguson.videosearch.analysis;

import com.github.willferguson.videosearch.analysis.analyzers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ResourceLoader;

/**
 * Created by will on 06/02/2017.
 */
@SpringBootApplication
@Configuration
@PropertySources({
        @PropertySource("classpath:google.properties"),
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:microsoft-credentials.properties")
})
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    ResourceLoader resourceLoader;


    @Value("${microsoft.vision.key}")
    String visionKey;
    @Value("${microsoft.vision.url}")
    String visionURL;
    @Value("${microsoft.emotion.key}")
    String emotionKey;
    @Value("${microsoft.emotion.url}")
    String emotionURL;
    @Value("${google.storage.bucket.name}")
    String googleBucketName;
    @Value("${google.application.name}")
    String googleApplicationName;

    @Bean
    public GoogleVisionAnalyzer googleVisionAnalyzer() {
        return new GoogleVisionAnalyzer(googleBucketName, googleApplicationName, resourceLoader);
    }

    @Bean
    public MicrosoftVisionAnalyzer microsoftVisionAnalyzer() {
        return new MicrosoftVisionAnalyzer(visionURL, visionKey);
    }

    @Bean
    MicrosoftEmotionAnalyzer microsoftEmotionAnalyzer() {
        return new MicrosoftEmotionAnalyzer(emotionURL, emotionKey);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfig() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(Application.class, args);

        applicationContext.getBeansOfType(ImageAnalyser.class)
                .forEach((name, analyser) -> {
                    //Register all except the aggregator.
                    //TODO This needs to be a little more clever that direct polymorphism.
                    if (!(analyser instanceof ImageAnalysisAggregator)) {
                        //TODO Check the fake flag
                        AnalyzerRegistry.register(analyser);
                    }

                });
    }
}
