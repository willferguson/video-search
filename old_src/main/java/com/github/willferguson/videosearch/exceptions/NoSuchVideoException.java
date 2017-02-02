package com.github.willferguson.videosearch.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by will on 28/10/2016.
 */
public class NoSuchVideoException extends RuntimeException {

    private static final Logger logger = LoggerFactory.getLogger(NoSuchVideoException.class);

    public NoSuchVideoException() {
        super();
    }

    public NoSuchVideoException(String message) {
        super(message);
    }

    public NoSuchVideoException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchVideoException(Throwable cause) {
        super(cause);
    }
}
