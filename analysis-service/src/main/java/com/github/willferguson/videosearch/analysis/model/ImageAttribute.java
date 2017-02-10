package com.github.willferguson.videosearch.analysis.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a single attribute of an image,
 * with the name of the attribute and it's confidence confidence (0-1).
 *
 * EG - Happy, 0.99
 * Created by will on 11/11/2016.
 *
 */
public class ImageAttribute {

    private String name;
    private double confidence;

    public ImageAttribute() {
    }

    public ImageAttribute(String name, double confidence) {
        this.name = name;
        this.confidence = confidence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    private static final Logger logger = LoggerFactory.getLogger(ImageAttribute.class);

    @Override
    public String toString() {
        return "FrameAttribute{" +
                "name='" + name + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}
