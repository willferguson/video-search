package com.github.willferguson.videosearch.search.persistence;

import com.github.willferguson.videosearch.search.model.Frame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

/**
 * Created by will on 02/11/2016.
 */
@Component
public interface FrameRepository extends ElasticsearchRepository<Frame, String> {

    Page<Frame> findByVideoId(String videoId, Pageable pageable);


}
