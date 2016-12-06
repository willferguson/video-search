package com.github.willferguson.videosearch;

import com.github.willferguson.videosearch.service.analysis.*;
import com.github.willferguson.videosearch.service.analysis.fake.AbstractDummyAnalyser;
import com.github.willferguson.videosearch.state.SimpleVideoStateManager;
import com.github.willferguson.videosearch.service.frame.ffmpeg.FFMpegFrameExtractionService;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import com.github.willferguson.videosearch.storage.ContentStore;
import com.github.willferguson.videosearch.storage.LocalContentStore;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;

/**
 * Central AppConfig - probably worth splitting out into layred versions at some point.
 *
 * TODO This needs cleaning up and splitting out etc
 * Created by will on 30/09/2016.
 */
@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.github.willferguson.videosearch.persistence.elastic")
@Configuration
@PropertySources({
        @PropertySource("classpath:google.properties"),
        @PropertySource("classpath:application.properties"),
        @PropertySource("classpath:microsoft-credentials.properties")
})
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

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

    @Autowired
    ResourceLoader resourceLoader;



    //TODO Change to load the dir from config server
    @Bean
    public FrameExtractionService frameExtractionService() {
        return new FFMpegFrameExtractionService(Paths.get("/Users/will/tmp/extraction"), new SimpleVideoStateManager());
    }

    //TODO Change to define the content store from config server
    @Bean
    public ContentStore contentStore() {
        return new LocalContentStore(Paths.get("/tmp", "frames"));
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchTemplate(client());
    }

    @Bean
    public Client client()  {
        TransportClient client= TransportClient.builder().build();
        try {
            TransportAddress address = new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300);
            client.addTransportAddress(address);
            return client;

        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

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
                        AnalyzerRegistry.register(analyser);
                    }

                });
    }
}
