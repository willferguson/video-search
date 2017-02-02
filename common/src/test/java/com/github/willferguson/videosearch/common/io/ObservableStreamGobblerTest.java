package com.github.willferguson.videosearch.common.io;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by will on 14/10/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= ObservableStreamGobblerTest.Config.class)
public class ObservableStreamGobblerTest {

    private static final Logger logger = LoggerFactory.getLogger(ObservableStreamGobblerTest.class);

    @Autowired
    ResourceLoader resourceLoader;

    @Test
    public void test() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Resource resource = resourceLoader.getResource("classpath:test.txt");


        AtomicInteger atomicInteger = new AtomicInteger(0);

        FileInputStream fileInputStream = new FileInputStream(resource.getFile());
        ObservableStreamGobbler.gobbleByLine(fileInputStream)
                .subscribe(
                        nextLine -> {
                            atomicInteger.incrementAndGet();
                            System.out.println(nextLine);
                        },
                        error -> {

                            error.printStackTrace();
                            latch.countDown();
                        },
                        () -> {
                            System.out.println("Done");
                            latch.countDown();
                        }
                );

        latch.await();
        Assert.assertEquals("Should have 10 lines", 10, atomicInteger.get());
    }

    @Configuration
    public static class Config {
    }
}
