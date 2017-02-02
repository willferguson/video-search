package com.github.willferguson.videosearch.search.service;

import com.github.willferguson.videosearch.search.model.SearchResult;
import com.github.willferguson.videosearch.search.persistence.ElasticFrameSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Single;

/**
 * Created by will on 07/12/2016.
 */
@Component
public class ElasticSearchService implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchService.class);

    private ElasticFrameSearchRepository frameSearchRepository;

    @Autowired
    public ElasticSearchService(ElasticFrameSearchRepository frameSearchRepository) {
        this.frameSearchRepository = frameSearchRepository;
    }

    @Override
    public Single<SearchResult> findAll(double confidence, int pageSize, int from) {
        return Single.fromCallable(() -> {
            return frameSearchRepository.searchByVideo(null, confidence, pageSize, from);
        });
    }

    @Override
    public Single<SearchResult> byVideo(String videoId, double confidence, int pageSize, int from) {
        return Single.fromCallable(() -> {
            return frameSearchRepository.searchByVideo(videoId, confidence, pageSize, from);
        });
    }

    @Override
    public Single<SearchResult> byAttributeName(String attributeName, double confidence, int pageSize, int from) {
        return null;
    }

    @Override
    public Single<SearchResult> byAttributeGroupName(String attributeGroupName, double confidence, int pageSize, int from) {
        return null;
    }
}
