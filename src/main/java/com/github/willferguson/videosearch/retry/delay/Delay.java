package com.github.willferguson.videosearch.retry.delay;

import java.util.concurrent.TimeUnit;

/**
 * Created by will on 01/07/2016.
 */
public abstract class Delay {

    private TimeUnit timeUnit;

    Delay(TimeUnit timeUnit) {

        this.timeUnit = timeUnit;
    }

    public static Delay linear(long initial, long step, TimeUnit timeUnit) {
        return new LinearDelay(initial, step, timeUnit);
    }

    public static Delay fixed(long delay, TimeUnit timeUnit) {
        return new FixedDelay(delay, timeUnit);
    }

    public static Delay exponential(long base, TimeUnit timeUnit) {
        return new ExponentialDelay(base, timeUnit);
    }

    public abstract long calculate(long attempt);

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
