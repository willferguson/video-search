package com.github.willferguson.videosearch.service.analysis.fake;

import com.github.willferguson.videosearch.service.analysis.ImageAnalyser;
import com.github.willferguson.videosearch.service.frame.utils.ObservableIOPipe;
import com.google.api.client.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Marker interface for all fake analysers.
 *
 * Created by will on 15/11/2016
 */
public abstract class AbstractDummyAnalyser implements ImageAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDummyAnalyser.class);

    /**
     * Reads the stream and discards the bytes.
     * @param inputStream
     */
    protected void sink(InputStream inputStream) {
        try {
            int i = -1;
            byte[] buffer = new byte[1024];
            while ((i = inputStream.read(buffer)) != -1) {
                //Do nothing with the bytes
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected double randomDouble() {
        return Math.random();
    }

    //Just some random ints between 1 - 10 (can't be bothered to do this properly)
    protected int randomInt() {
        return 1 + (int)Math.floor(Math.random() * 9);
    }
}
