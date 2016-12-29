package com.github.willferguson.videosearch.service.search;

import com.github.willferguson.videosearch.model.search.SearchResult;
import com.github.willferguson.videosearch.persistence.elastic.FrameRepository;
import com.github.willferguson.videosearch.persistence.elastic.FrameSearchRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import rx.Single;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Created by will on 07/12/2016.
 */
@Component
public class ElasticSearchService implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchService.class);

    private FrameSearchRepository frameSearchRepository;

    @Autowired
    public ElasticSearchService(FrameSearchRepository frameSearchRepository) {
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
