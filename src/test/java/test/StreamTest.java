package test;

import com.github.willferguson.videosearch.service.frame.ffmpeg.ObservableStreamGobbler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created by will on 14/10/2016.
 */
public class StreamTest {

    private static final Logger logger = LoggerFactory.getLogger(StreamTest.class);

    @Test
    public void test() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        File file = new File("/Users/will/ffmpeg/test.txt");
        FileInputStream fileInputStream = new FileInputStream(file);
        ObservableStreamGobbler.gobble(fileInputStream)
                .subscribe(
                        nextLine -> System.out.println(nextLine),
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
    }
}
