package com.github.willferguson.videosearch.persistence.elastic;

import com.github.willferguson.videosearch.model.AttributeGroup;
import com.github.willferguson.videosearch.model.search.FrameAggregation;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
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

import java.util.*;

import static org.bouncycastle.crypto.tls.ConnectionEnd.client;

/**
 * TODO This needs to be merged with the FrameRepository
 * Created by will on 24/11/2016.
 */
@Component
public class FrameAggregations {

    private static final Logger logger = LoggerFactory.getLogger(FrameAggregations.class);
    private ElasticsearchOperations elasticsearchTemplate;

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
    public FrameAggregation buildAggregations() {

        TermsBuilder attributesNameTermBuilder = AggregationBuilders.terms("attributes_name")
                .field("metadata.attributes.name");

        TermsBuilder nameTermsBuilder = AggregationBuilders.terms("name")
                .field("metadata.name")
                .subAggregation(attributesNameTermBuilder);

        NestedBuilder metadataTermsBuilder = AggregationBuilders.nested("metadata")
                .path("metadata")
                .subAggregation(nameTermsBuilder);


        SearchResponse response = elasticsearchTemplate.getClient().prepareSearch("video")
                .setTypes("frame")
                .addAggregation(metadataTermsBuilder)
                .execute().actionGet();

        //TODO This needs to be converted to n depth recursion.
        Map<String, Aggregation> aggregations = response.getAggregations().asMap();
        Nested nestedAggregations = (Nested) aggregations.get("metadata");
        long overallCount = nestedAggregations.getDocCount();

        StringTerms nameStringTerms = (StringTerms) nestedAggregations.getAggregations().asMap().get("name");


        FrameAggregation frameAggregation = new FrameAggregation();
        frameAggregation.setName("all");
        frameAggregation.setCount(Long.toString(overallCount));

        nameStringTerms.getBuckets()
                .forEach(bucket -> {
                    String name = (String) bucket.getKey(); //EG Dummy
                    long count = bucket.getDocCount(); //eg 54
                    FrameAggregation typeAggregation = new FrameAggregation();
                    typeAggregation.setName(name);
                    typeAggregation.setCount(Long.toString(count));

                    frameAggregation.addChildAggregation(typeAggregation);

                    StringTerms attributeNameTerms = (StringTerms) bucket.getAggregations().asMap().get("attributes_name");
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
}
