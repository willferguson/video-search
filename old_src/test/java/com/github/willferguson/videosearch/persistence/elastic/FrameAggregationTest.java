package com.github.willferguson.videosearch.persistence.elastic;

import com.github.willferguson.videosearch.model.search.FrameAggregation;
import com.github.willferguson.videosearch.model.search.SearchResult;
import com.github.willferguson.videosearch.service.search.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by will on 24/11/2016.
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class FrameAggregationTest {

    private static final Logger logger = LoggerFactory.getLogger(FrameAggregationTest.class);

    @Autowired
    SearchService searchService;
    @Test
    public void test() {
        SearchResult searchResult = searchService.findAll(0.0, 10, 0).toBlocking().value();
        System.out.println(searchResult.toJsonString());

    }
}
