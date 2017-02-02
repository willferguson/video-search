package com.github.willferguson.videosearch.common.retry.delay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by will on 01/07/2016.
 */
public class ExponentialDelay extends Delay {

    private static final Logger logger = LoggerFactory.getLogger(ExponentialDelay.class);
    private long base;

    ExponentialDelay(long base, TimeUnit timeUnit) {
        super(timeUnit);
        this.base = base;
    }

    @Override
    public long calculate(long attempt) {
        if (attempt == 1) {
            return base;
        }
        return calculateInternal(attempt -2, base);

    }

    private long calculateInternal(long remainingIterations, long value) {
        long shiftedValue = value << 1;
        if (shiftedValue < value) return Long.MAX_VALUE; //if we've hit the max value return
        if (remainingIterations == 0) { //last iteration
            return shiftedValue;
        }
        return calculateInternal(remainingIterations -1, shiftedValue);
    }
}
