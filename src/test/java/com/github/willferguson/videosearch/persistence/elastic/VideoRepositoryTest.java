package com.github.willferguson.videosearch.persistence.elastic;

import com.github.willferguson.videosearch.Application;
import com.github.willferguson.videosearch.model.Status;
import com.github.willferguson.videosearch.model.Video;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * Created by will on 01/11/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= VideoRepositoryTest.Config.class)
public class VideoRepositoryTest {

    private static final Logger logger = LoggerFactory.getLogger(VideoRepositoryTest.class);

    @Autowired
    VideoRepository videoRepository;

    @Before
    public void setup() {

        Video video1 = new Video();
        video1.setUuid("1");
        video1.setStatus(Status.STORED);

        Video video2 = new Video();
        video2.setUuid("2");
        video2.setStatus(Status.FAILED);

        videoRepository.save(video1);
        videoRepository.save(video2);

    }

    @Test
    public void testFindById() {
        Video video = videoRepository.findOne("1");
        Assert.assertEquals(Status.STORED, video.getStatus());
    }

    @Test
    public void testFindByStatus() {
        Page<Video> videoPage = videoRepository.findByStatus(Status.FAILED, new PageRequest(0, 10));
        Assert.assertEquals(1, videoPage.getNumberOfElements());
        Status status = videoPage.getContent().get(0).getStatus();
        Assert.assertEquals(Status.FAILED, status);

    }


    @Configuration
    @ComponentScan(basePackages = {"com.github.willferguson.videosearch.persistence.elastic"})
    @EnableElasticsearchRepositories(basePackages = "com.github.willferguson.videosearch.persistence.elastic")
    public static class Config {

        private static final String ELASTIC_SEARCH_HOME_DIR = "/opt/elasticsearch-2.4.0";
        private static final String ELASTIC_SEARCH_DATA_DIR = "/opt/elasticsearch-2.4.0/data";


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

    }
}

