package com.github.willferguson.videosearch.search.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Think of a better name
 * Created by will on 09/12/2016.
 */
public class SearchParam {

    private static final Logger logger = LoggerFactory.getLogger(SearchParam.class);

    public SearchParam(String fieldName, String fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    private String fieldName;
    private String fieldValue;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}
