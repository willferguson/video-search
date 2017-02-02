package com.github.willferguson.videosearch.common.retry.delay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * Created by will on 01/07/2016.
 */
public class LinearDelay extends Delay {

    private static final Logger logger = LoggerFactory.getLogger(LinearDelay.class);
    private long initial;
    private long step;

    public LinearDelay(long initial, long step, TimeUnit timeUnit) {
        super(timeUnit);
        this.initial = initial;
        this.step = step;
    }

    @Override
    public long calculate(long attempt) {
        if (attempt == 1L) {
            return initial;
        }
        else {
            long result = Math.round((double)initial + (step * (attempt -1)));
            return result;
        }
    }
}
