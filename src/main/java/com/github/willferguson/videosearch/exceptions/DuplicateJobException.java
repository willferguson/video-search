package com.github.willferguson.videosearch.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indicates that a job with this uuid has been submitted before
 * Created by will on 29/10/2016.
 */
public class DuplicateJobException extends RuntimeException {

    private static final Logger logger = LoggerFactory.getLogger(DuplicateJobException.class);

    public DuplicateJobException() {
        super();
    }

    public DuplicateJobException(String message) {
        super(message);
    }

    public DuplicateJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateJobException(Throwable cause) {
        super(cause);
    }
}
