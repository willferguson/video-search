package com.github.willferguson.videosearch.search.persistence;

import com.github.willferguson.videosearch.search.model.Status;
import com.github.willferguson.videosearch.search.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

/**
 * Created by will on 31/10/2016.
 */
@Component
public interface VideoRepository extends ElasticsearchRepository<Video, String> {

    Page<Video> findByStatus(Status status, Pageable pageable);

}
