package test.retry;

import com.github.willferguson.videosearch.retry.delay.Delay;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by will on 01/07/2016.
 */
public class DelayTest {

    private static final Logger logger = LoggerFactory.getLogger(DelayTest.class);

    @Test
    public void fixedDelayTest() {
        Delay delay = Delay.fixed(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(100, delay.calculate(1));
        Assert.assertEquals(100, delay.calculate(2));
        Assert.assertEquals(100, delay.calculate(3));
    }

    @Test
    public void linearDelayTest() {
        Delay delay = Delay.linear(100, 50, TimeUnit.MILLISECONDS);
        Assert.assertEquals(100, delay.calculate(1));
        Assert.assertEquals(150, delay.calculate(2));
        Assert.assertEquals(200, delay.calculate(3));
    }

    @Test
    public void expDelayTest() {
        Delay delay = Delay.exponential(10, TimeUnit.MILLISECONDS);
        Assert.assertEquals(10, delay.calculate(1));
        Assert.assertEquals(20, delay.calculate(2));
        Assert.assertEquals(40, delay.calculate(3));
    }

    @Test
    public void wrappingTest() {
        Delay delay = Delay.exponential(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        Assert.assertEquals(Long.MAX_VALUE, delay.calculate(1));
        Assert.assertEquals(Long.MAX_VALUE, delay.calculate(2));

        delay = Delay.linear(Long.MAX_VALUE, 50, TimeUnit.MILLISECONDS);
        Assert.assertEquals(Long.MAX_VALUE, delay.calculate(1));
        Assert.assertEquals(Long.MAX_VALUE, delay.calculate(2));
    }
}
