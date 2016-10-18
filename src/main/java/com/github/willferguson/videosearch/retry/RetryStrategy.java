package com.github.willferguson.videosearch.retry;

import com.github.willferguson.videosearch.retry.delay.Delay;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by will on 20/06/2016.
 */
public class RetryStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RetryStrategy.class);
    private Function<Throwable, Boolean> shouldRetryFunction = throwable -> false;
    private int numberOfRetries = 1;
    private Delay delay = Delay.fixed(1, TimeUnit.SECONDS);

    private RetryStrategy() {
    }

    private void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    private void setShouldRetryFunction(Function<Throwable, Boolean> shouldRetryFunction) {
        this.shouldRetryFunction = shouldRetryFunction;
    }

    public static Builder builder() {
        return new Builder();
    }



    public Observable<?> run(Observable<? extends Throwable> errorObservable) {
        return errorObservable.zipWith(Observable.range(1, numberOfRetries + 1), (throwable, attempt) -> {
            if (attempt == numberOfRetries + 1) {
                logger.info("No more retries available - propagating error");
                throw Throwables.propagate(throwable);
            }
            if (shouldRetryFunction.apply(throwable)) {
                return attempt;
            }
            else {
                throw Throwables.propagate(throwable);
            }
        })
        .flatMap(attempt -> {
            long delayDuration = delay.calculate(attempt);
            logger.trace("Delaying retry by " + delayDuration + " " + delay.getTimeUnit().toString());
            return Observable.timer(delayDuration, delay.getTimeUnit());
        });
    }

    private void setDelay(Delay delay) {
        this.delay = delay;
    }


    public static class Builder {
        RetryStrategy retryStrategy;

        private Builder() {
            retryStrategy = new RetryStrategy();
        }

        /**
         * How many times should we try to retry before propagating the exception
         * @param retries Number of retries.
         * @return an instance of this so methods can be chained.
         */
        public Builder times(int retries) {
            retryStrategy.setNumberOfRetries(retries);
            return this;
        }

        /**
         * When do we want to retry
         * @param predicate Determines whether we should retry based on the passed exception.
         * Only retries on exceptions that satisfy the predicate.
         * @return an instance of this so methods can be chained.
         */
        public Builder when(Function<Throwable, Boolean> predicate) {
            retryStrategy.setShouldRetryFunction(predicate);
            return this;
        }
        public Builder delay(Delay delay) {
            retryStrategy.setDelay(delay);
            return this;
        }
        public RetryStrategy build() {
            return retryStrategy;
        }

    }
}
