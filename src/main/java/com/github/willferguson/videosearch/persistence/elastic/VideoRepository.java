package com.github.willferguson.videosearch.persistence.elastic;

import com.github.willferguson.videosearch.model.Status;
import com.github.willferguson.videosearch.model.Video;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
