/**
 * Unit tests for AffinitizedExecutorService.
 * <p>
 * Mocks thread factory and runnable tasks. Verifies correct construction and task submission logic.
 * </p>
 * Author: kgoldstein
 * Date: Sep 29, 2025
 */

package com.gcs.tools.concurrency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;


@ExtendWith(MockitoExtension.class)
class AffinitizedExecutorServiceTest {

    @Mock
    private AffinitizedThreadFactory threadFactoryMock;

    @Mock
    private Runnable runnableMock;

    @InjectMocks
    private AffinitizedExecutorService executorService;





    @BeforeEach
    public void setup() {
        executorService = new AffinitizedExecutorService(2, threadFactoryMock);
    }





    @Test
    void testConstructor_initializesThreadPool() {
        assertEquals(2, executorService.getCorePoolSize());
        assertEquals(2, executorService.getMaximumPoolSize());
        assertEquals(0, executorService.getKeepAliveTime(TimeUnit.MILLISECONDS));
    }





    /**
     * Since the actual execution is asynchronous, we can't directly verify the runnable was executed with affinity.
     * However, we can check that the task was submitted to the executor's queue.
     */
    @Test
    public void testSubmitAffinitizedTask_submitsRunnableWithAffinity() {
        int cpuAffinity = 1;
        executorService.submitAffinitizedTask(runnableMock, cpuAffinity);
        assertEquals(1, executorService.getQueue().size());
    }





    @Test
    public void testSubmitAffinitizedTask_defaultAffinity() {
        executorService.submitAffinitizedTask(runnableMock, 0);
        assertEquals(1, executorService.getQueue().size());
    }





    @Test
    public void testShutdownExecutorService() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        assertTrue(executorService.isShutdown());
    }





    @Test
    public void testSubmitAffinitizedTask_withThreadName() {
        String threadName = "custom-thread";
        int cpuAffinity = 2;
        final var fut = executorService.submitAffinitizedTask(threadName, runnableMock, cpuAffinity);
        assertNotNull(fut);
        assertEquals(1, executorService.getQueue().size());
    }





    @Test
    public void testSubmitAffinitizedTask_withThreadNameAndDefaultAffinity() {
        String threadName = "custom-thread-default-affinity";
        executorService.submitAffinitizedTask(threadName, runnableMock, 0);
        assertEquals(1, executorService.getQueue().size());
    }





    @Test
    public void testSubmitMultipleAffinitizedTasks() {
        for (int i = 0; i < 5; i++) {
            final var exec = executorService.submitAffinitizedTask(runnableMock, i % 2);
            assertNotNull(exec);
        }
        assertEquals(5, executorService.getQueue().size());

    }
}
