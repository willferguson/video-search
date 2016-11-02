package com.github.willferguson.videosearch.persistence.elastic;

import com.github.willferguson.videosearch.model.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

/**
 * Created by will on 02/11/2016.
 */
@Component
public interface FrameRepository extends ElasticsearchRepository<Frame, String> {


}
