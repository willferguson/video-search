package com.github.willferguson.videosearch.service.frame.utils;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;

/**
 * Created by will on 19/10/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes= ObservableIOPipeTest.Config.class)
public class ObservableIOPipeTest {

    private static final Logger logger = LoggerFactory.getLogger(ObservableIOPipeTest.class);

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void testTraditional() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String string = "1, 2, 3, 4, 5";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(string.getBytes());

        ObservableIOPipe.traditionalPipe(byteArrayInputStream, byteArrayOutputStream)
                .toObservable().toBlocking().subscribe();

        byte[] output = byteArrayOutputStream.toByteArray();

        Assert.assertArrayEquals(string.getBytes(), output);

    }

    @Test
    public void testRx() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String input = "1, 2, 3, 4, 5";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input.getBytes());

        ObservableIOPipe.rxPipe(byteArrayInputStream, byteArrayOutputStream)
                .toObservable().toBlocking().subscribe();

        byte[] output = byteArrayOutputStream.toByteArray();

        Assert.assertArrayEquals(input.getBytes(), output);

    }

    @Test
    public void testLarge() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:preview.mp4");
        File file = new File("/tmp/out.mp4");
        file.createNewFile();
        ObservableIOPipe.rxPipe(resource.getInputStream(), new FileOutputStream(file))
                .toObservable().toBlocking().subscribe();

    }
    @Configuration
    public static class Config {
    }
}
