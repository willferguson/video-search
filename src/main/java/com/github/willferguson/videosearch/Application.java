package com.github.willferguson.videosearch;

import com.github.willferguson.videosearch.service.analysis.AnalyzerRegistry;
import com.github.willferguson.videosearch.service.analysis.ImageAnalyser;
import com.github.willferguson.videosearch.state.SimpleVideoStateManager;
import com.github.willferguson.videosearch.service.frame.ffmpeg.FFMpegFrameExtractionService;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import com.github.willferguson.videosearch.storage.ContentStore;
import com.github.willferguson.videosearch.storage.LocalContentStore;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;

/**
 * Central AppConfig - probably worth splitting out into layred versions at some point.
 * Created by will on 30/09/2016.
 */
@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.github.willferguson.videosearch.persistence.elastic")
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static final String ELASTIC_SEARCH_HOME_DIR = "/opt/elasticsearch-2.4.0";
    private static final String ELASTIC_SEARCH_DATA_DIR = "/opt/elasticsearch-2.4.0/data";

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

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(Application.class, args);
        applicationContext.getBeansOfType(ImageAnalyser.class)
                .forEach((name, analyser) -> {
                    AnalyzerRegistry.register(analyser);
                });
    }
}
