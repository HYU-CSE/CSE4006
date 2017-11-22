package thread;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class PoolTest {
    private static final int nThreads = 8;
    private static final int nTests = 1000000;
    private static Pool pool;

    @Before
    public void makeInstance() throws Exception {
        pool = new Pool(nThreads);
    }

    @Test
    public void push() throws Exception {
        AtomicInteger count = new AtomicInteger();

        for (int i = 0; i < nTests; ++i) {
            pool.push(count::getAndIncrement);
        }

        pool.join();
        assertEquals(nTests, count.get());
    }

    @Test
    public void time() throws Exception {
        long time = System.currentTimeMillis();
        {
            AtomicInteger count = new AtomicInteger();

            for (int i = 0; i < nTests; ++i) {
                pool.push(count::getAndIncrement);
            }
        }
        long poolTime = System.currentTimeMillis() - time;

        time = System.currentTimeMillis();
        {
            AtomicInteger count = new AtomicInteger();

            for (int i = 0; i < nTests; ++i) {
                count.getAndIncrement();
            }
        }
        long defaultTime = System.currentTimeMillis() - time;

        System.out.println(poolTime);
        System.out.println(defaultTime);
    }
}