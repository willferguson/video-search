package com.github.willferguson.videosearch;

import com.github.willferguson.videosearch.service.analysis.AnalyzerRegistry;
import com.github.willferguson.videosearch.service.analysis.ImageAnalyser;
import com.github.willferguson.videosearch.state.SimpleVideoStateManager;
import com.github.willferguson.videosearch.service.frame.ffmpeg.FFMpegFrameExtractionService;
import com.github.willferguson.videosearch.service.frame.FrameExtractionService;
import com.github.willferguson.videosearch.storage.ContentStore;
import com.github.willferguson.videosearch.storage.LocalContentStore;
import org.elasticsearch.common.settings.Settings;
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

import java.nio.file.Paths;

/**
 * Central AppConfig - probably worth splitting out into layred versions at some point.
 * Created by will on 30/09/2016.
 */
@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.github.willferguson.videosearch.persistence.elastic")
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static final String ELASTIC_SEARCH_HOME_DIR = "/opt/elasticsearch";
    private static final String ELASTIC_SEARCH_DATA_DIR = "/opt/elasticsearch/data";

    //TODO Change to load the dir from config server
    @Bean
    public FrameExtractionService frameExtractionService() {
        return new FFMpegFrameExtractionService(Paths.get("Users/will/tmp"), new SimpleVideoStateManager());
    }

    //TODO Change to define the content store from config server
    @Bean
    public ContentStore contentStore(ResourceLoader resourceLoader) {
        return new LocalContentStore(Paths.get("/tmp", "frames"), resourceLoader);
    }


    @Bean
    public NodeBuilder nodeBuilder() {
        return new NodeBuilder();
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        Settings.Builder elasticsearchSettings =
                Settings.settingsBuilder()
                        .put("http.enabled", "false")
                        .put("path.data", ELASTIC_SEARCH_DATA_DIR)
                        .put("path.home", ELASTIC_SEARCH_HOME_DIR);

        return new ElasticsearchTemplate(nodeBuilder()
                .local(true)
                .settings(elasticsearchSettings.build())
                .node()
                .client());
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(Application.class, args);
        applicationContext.getBeansOfType(ImageAnalyser.class)
                .forEach((name, analyser) -> {
                    AnalyzerRegistry.register(analyser);
                });
    }
}
