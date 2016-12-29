package com.github.willferguson.videosearch.service.search;

import com.github.willferguson.videosearch.model.search.SearchResult;
import rx.Single;

/**
 * Defines all the various search operations
 * Created by will on 07/12/2016.
 */
public interface SearchService {


    /**
     * Returns all results with a confidence level greater than passed.
     * @param confidence The minimum confidence level to return
     * @param pageSize The size of the result set
     * @param from The result to start from (0 indexed)
     * @return A Single emitting a single SearchResult object containing all Frames, and all aggregations.
     */
    public Single<SearchResult> findAll(double confidence, int pageSize, int from);

    /**
     * Returns hits for the specific video, with a confidence level greater than that passed.
     * @param videoId The video to search in
     * @param confidence The minumum confidence level to return
     * @param pageSize The size of the result set
     * @param from The result to start from (0 indexed)
     * @return A Single emitting a single SearchResult object containing all Frames, and all aggregations.
     */
    public Single<SearchResult> byVideo(String videoId, double confidence, int pageSize, int from);

    /**
     * Returns hits for the specific attribute name.
     * @param attributeName The name of the attribute - EG Happy, Marmite
     * @param confidence
     * @param pageSize
     * @param from
     * @return
     */
    public Single<SearchResult> byAttributeName(String attributeName, double confidence, int pageSize, int from);

    /**
     * Returns hits for the specific attribute name.
     * @param attributeGroupName The attribite group mame - eg Logo or Landscape
     * @param confidence
     * @param pageSize
     * @param from
     * @return
     */
    public Single<SearchResult> byAttributeGroupName(String attributeGroupName, double confidence, int pageSize, int from);




}
