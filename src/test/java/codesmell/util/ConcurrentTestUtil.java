package codesmell.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Copied from https://github.com/junit-team/junit/wiki/Multithreaded-code-and-concurrency
 */
public class ConcurrentTestUtil {

    public static void assertConcurrent(final String message, final List<? extends Runnable> runnables, final int maxTimeoutSeconds, final int numExceptionsExpected) throws InterruptedException {
        final int numThreads = runnables.size();
        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        try {
            final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
            final CountDownLatch afterInitBlocker = new CountDownLatch(1);
            final CountDownLatch allDone = new CountDownLatch(numThreads);
            for (final Runnable submittedTestRunnable : runnables) {
                threadPool.submit(new Runnable() {
                    public void run() {
                        // notify that this thread is ready
                        allExecutorThreadsReady.countDown();
                        try {
                            // wait until all the init blocker is removed
                            afterInitBlocker.await();
                            // run the test
                            submittedTestRunnable.run();
                        } catch (final Throwable e) {
                            exceptions.add(e);
                        } finally {
                            allDone.countDown();
                        }
                    }
                });
            }
            // wait until all threads are ready
            assertTrue(allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
            // start the threads by setting init blocker to zero
            afterInitBlocker.countDown();
            // wait until all threads complete processing
            assertTrue(allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
        } finally {
            threadPool.shutdownNow();
        }
        assertEquals(numExceptionsExpected, exceptions.size());
    }


    /**
     * Test to verify assertConcurrent() works and not have any deadlocks in itself.
     */
    @Test
    public void testAssertConcurrrent() throws InterruptedException {
        final TestMock mock = mock(TestMock.class);
        when(mock.tested()).thenReturn(true);

        int NUM_REQUESTS = 200;
        List<Runnable> requests = new ArrayList<Runnable>(NUM_REQUESTS);
        for (int i = 0; i < NUM_REQUESTS; i++) {
            requests.add(new Runnable() {
                public void run() {
                    assertTrue("tested should be true", mock.tested());
                }
            });
        }

        int maxTimeoutSeconds = 30;
        assertConcurrent("", requests, maxTimeoutSeconds, 0);

        verify(mock, times(NUM_REQUESTS)).tested();
    }

    interface TestMock {
        boolean tested();
    }
}