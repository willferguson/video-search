package com.github.willferguson.videosearch.persistence.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.willferguson.videosearch.model.AttributeGroup;
import com.github.willferguson.videosearch.model.Frame;
import com.github.willferguson.videosearch.model.search.FrameAggregation;
import com.github.willferguson.videosearch.model.search.SearchResult;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.nested.InternalNested;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.bouncycastle.crypto.tls.ConnectionEnd.client;

/**
 * TODO This needs to be merged with the FrameRepository
 * Created by will on 24/11/2016.
 */
@Component
public class FrameAggregations {

    private static final Logger logger = LoggerFactory.getLogger(FrameAggregations.class);
    private ElasticsearchOperations elasticsearchTemplate;

    private static final String CONFIDENCE_FILTER = "confidence_filter";
    private static final String METADATA = "metadata";
    private static final String ATTRIBUTE_GROUP_NAME = "attribute_group_name";
    private static final String ATTRIBUTES = "attributes";
    private static final String ATTRIBUTE_NAME = "attribute_name";

    @Autowired
    public FrameAggregations(ElasticsearchOperations elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }


    /**
     * Builds out terms like the following.
     * TODO - Should take some search param too.
     * GET /video/frame/_search
     *   {
     *     "aggs": {
     *       "metadata": {
     *           "nested": {
     *           "path": "metadata"
     *         },
     *         "aggs": {
     *             "tag_name":{
     *                 "terms":{
     *                     "field": "metadata.name"
     *                 },
     *                 "aggs": {
     *                     "attribute_name": {
     *                         "terms": {
     *                             "field": "metadata.attributes.name"
     *                         }
     *                     }
     *                 }
     *             }
     *         }
     *       }
     *     }
     *   }
     */
    public SearchResult buildAggregations(String videoId, double confidence, int size, int from) {

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

        QueryBuilder queryBuilder;
        if (videoId != null) {
            queryBuilder = QueryBuilders.matchQuery("videoId", videoId);
        }
        else {
            queryBuilder = QueryBuilders.matchAllQuery();
        }
        SearchResponse response = elasticsearchTemplate.getClient().prepareSearch("video")
                .setTypes("frame")
                .setQuery(queryBuilder)
                .addAggregation(metadataTermsBuilder)
                .execute().actionGet();

        ObjectMapper objectMapper = new ObjectMapper();
        List<Frame> hits = StreamSupport.stream(response.getHits().spliterator(), true)
                .map(searchHitFields -> searchHitFields.getSourceAsString())
                .map(jsonFrame -> {
                    try {
                        return objectMapper.readValue(jsonFrame, Frame.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        //TODO This needs to be converted to n depth recursion.
        Map<String, Aggregation> aggregations = response.getAggregations().asMap();
        Nested nestedMetadata = (Nested) aggregations.get(METADATA);
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

        SearchResult searchResult = new SearchResult();
        searchResult.setFrameHits(hits);
        searchResult.setFrameAggregation(frameAggregation);

        return searchResult;


    }
}
