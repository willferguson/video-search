package com.github.willferguson.videosearch.search.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Set;

/**
 * Created by will on 29/11/2016.
 */

public class AttributeGroup {

    private static final Logger logger = LoggerFactory.getLogger(AttributeGroup.class);

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String name;
    @Field(type = FieldType.Nested)
    private Set<FrameAttribute> attributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<FrameAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<FrameAttribute> attributes) {
        this.attributes = attributes;
    }
}
