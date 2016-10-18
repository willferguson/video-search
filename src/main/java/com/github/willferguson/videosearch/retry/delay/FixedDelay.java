package com.github.willferguson.videosearch.retry.delay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * TODO Assumes that all delays are in ms. Convert
 * Created by will on 01/07/2016.
 */
class FixedDelay extends Delay {

    private static final Logger logger = LoggerFactory.getLogger(FixedDelay.class);
    private long delay;

    FixedDelay(long delay, TimeUnit timeUnit) {
        super(timeUnit);
        this.delay = delay;
    }

    @Override
    public long calculate(long attempt) {
        return delay;
    }
}
