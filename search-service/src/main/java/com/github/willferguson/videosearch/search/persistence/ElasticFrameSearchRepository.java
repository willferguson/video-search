package com.github.willferguson.videosearch.search.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.willferguson.videosearch.search.model.Frame;
import com.github.willferguson.videosearch.search.model.FrameAggregation;
import com.github.willferguson.videosearch.search.model.SearchParam;
import com.github.willferguson.videosearch.search.model.SearchResult;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * TODO - Extract to interface
 * TODO - Remove nullable and overload
 * Created by will on 07/12/2016.
 */
@Component
public class ElasticFrameSearchRepository {

    private static final Logger logger = LoggerFactory.getLogger(ElasticFrameSearchRepository.class);

    private static final String CONFIDENCE_FILTER = "confidence_filter";
    private static final String METADATA = "metadata";
    private static final String ATTRIBUTE_GROUP_NAME = "attribute_group_name";
    private static final String ATTRIBUTES = "attributes";
    private static final String ATTRIBUTE_NAME = "attribute_name";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private Client client;

    @Autowired
    public ElasticFrameSearchRepository(Client client) {
        this.client = client;
    }

    /**
     * Searches for frames with attributes above the minimum confidence
     * @param videoId The video to search for or null for all.
     * @param confidence The minimum confidence level for results.
     * @param pageSize The size of the result set returned
     * @param from The starting index for results.
     * @return
     */
    public SearchResult searchByVideo(@Nullable String videoId, double confidence, int pageSize, int from) {
        QueryBuilder queryBuilder = constructSearchQuery(new SearchParam("videoId", videoId));
        return searchInternal(queryBuilder, confidence, pageSize, from);
    }


    public SearchResult byAttributeName(String attributeName, double confidence, int pageSize, int from) {
        QueryBuilder queryBuilder = constructSearchQuery(new SearchParam("metadata.attributes.name", attributeName));
        return searchInternal(queryBuilder, confidence, pageSize, from);
    }

    public SearchResult byAttributeGroupName(String attributeGroupName, double confidence, int pageSize, int from) {
        QueryBuilder queryBuilder = constructSearchQuery(new SearchParam("metadata.name", attributeGroupName));
        return searchInternal(queryBuilder, confidence, pageSize, from);
    }


    private SearchResult searchInternal(QueryBuilder queryBuilder, double confidence, int pageSize, int from) {
        AbstractAggregationBuilder aggregationBuilder = buildAggregations(confidence);
        SearchResponse response = client.prepareSearch("video")
                .setTypes("frame")
                .setQuery(queryBuilder)
                .setSize(pageSize)
                .setFrom(from)
                .addAggregation(aggregationBuilder)
                .execute().actionGet();

        return processResponse(response);
    }

    private SearchResult processResponse(SearchResponse response) {
        SearchResult result = new SearchResult();
        result.setFrameHits(constructFrames(response.getHits()));
        result.setFrameAggregation(constructAggregation(response.getAggregations()));
        return result;
    }

    //TODO Refactor to something not so insane
    private FrameAggregation constructAggregation(Aggregations aggregations) {
        Map<String, Aggregation> aggregationMap = aggregations.asMap();
        Nested nestedMetadata = (Nested) aggregationMap.get(METADATA);
        long overallCount = nestedMetadata.getDocCount();

        Terms attributeGroupName = (Terms) nestedMetadata.getAggregations().asMap().get(ATTRIBUTE_GROUP_NAME);

        FrameAggregation frameAggregation = new FrameAggregation();
        frameAggregation.setName("all");
        frameAggregation.setCount(Long.toString(overallCount));

        attributeGroupName.getBuckets()
                .forEach(bucket -> {
                    String name = (String) bucket.getKey(); //EG Dummy
                    long count = bucket.getDocCount(); //eg 54
                    FrameAggregation typeAggregation = new FrameAggregation();
                    typeAggregation.setName(name);
                    typeAggregation.setCount(Long.toString(count));

                    frameAggregation.addChildAggregation(typeAggregation);

                    Nested nestedAttributes = bucket.getAggregations().get(ATTRIBUTES);

                    Filter filteredTerms = (Filter) nestedAttributes.getAggregations().asMap().get(CONFIDENCE_FILTER);

                    Terms attributeNameTerms = (Terms) filteredTerms.getAggregations().asMap().get(ATTRIBUTE_NAME);

                    attributeNameTerms.getBuckets()
                            .forEach(attributeBucket -> {
                                String attributeKey = (String) attributeBucket.getKey(); //EG Dummy1
                                long attributeCount = attributeBucket.getDocCount(); //eg 54

                                FrameAggregation attributeAggregation = new FrameAggregation();
                                attributeAggregation.setName(attributeKey);
                                attributeAggregation.setCount(Long.toString(attributeCount));

                                typeAggregation.addChildAggregation(attributeAggregation);
                            });
                });
        return frameAggregation;
    }

    private List<Frame> constructFrames(SearchHits hits) {
        return StreamSupport.stream(hits.spliterator(), true)
                .map(SearchHit::getSourceAsString)
                .map(jsonFrame -> {
                    try {
                        return objectMapper.readValue(jsonFrame, Frame.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns a querybuilder which matches the field or all if null.
     * @param searchParam Search Parameters - name, and value
     * @return
     */
    private QueryBuilder constructSearchQuery(@Nullable SearchParam searchParam) {
        return Optional.ofNullable(searchParam)
                .map(_searchParam -> (QueryBuilder)QueryBuilders.matchQuery(_searchParam.getFieldName(), _searchParam.getFieldValue()))
                .orElse(QueryBuilders.matchAllQuery());
    }


    //TODO - Refactor - there must be an affinity between this and equally insane response parsing above
    private AbstractAggregationBuilder buildAggregations(double confidence) {

        TermsBuilder attributesNameTermBuilder = AggregationBuilders.terms(ATTRIBUTE_NAME)
                .field("metadata.attributes.name");

        FilterAggregationBuilder confidenceFilterBuilder = AggregationBuilders.filter(CONFIDENCE_FILTER)
                .filter(QueryBuilders.rangeQuery("metadata.attributes.confidence").gt(confidence))
                .subAggregation(attributesNameTermBuilder);

        NestedBuilder attributesNestedBuilder = AggregationBuilders.nested(ATTRIBUTES)
                .path("metadata.attributes")
                .subAggregation(confidenceFilterBuilder);

        TermsBuilder nameTermsBuilder = AggregationBuilders.terms(ATTRIBUTE_GROUP_NAME)
                .field("metadata.name")
                .subAggregation(attributesNestedBuilder);

        NestedBuilder metadataTermsBuilder = AggregationBuilders.nested(METADATA)
                .path("metadata")
                .subAggregation(nameTermsBuilder);

        return metadataTermsBuilder;


    }




}
