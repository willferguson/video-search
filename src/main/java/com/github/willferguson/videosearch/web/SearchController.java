package com.github.willferguson.videosearch.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by will on 02/12/2016.
 */
@Controller
@RequestMapping("/video")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    /**
     * I have not idea what I'm returning yet! :)
     */
    public ResponseEntity<?> fetchAggregations() {
        return null;
    }
}
