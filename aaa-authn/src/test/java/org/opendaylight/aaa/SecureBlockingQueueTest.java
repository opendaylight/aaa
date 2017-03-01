/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.SecureBlockingQueue.SecureData;
import org.opendaylight.aaa.api.Authentication;

public class SecureBlockingQueueTest {
    private static final int MAX_TASKS = 100;

    @Before
    public void setup() {
        AuthenticationManager.instance().clear();
    }

    @Test
    public void testSecureThreadPoolExecutor() throws InterruptedException, ExecutionException {
        BlockingQueue<Runnable> queue = new SecureBlockingQueue<>(
                new ArrayBlockingQueue<SecureData<Runnable>>(10));
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 500, TimeUnit.MILLISECONDS,
                queue);
        executor.prestartAllCoreThreads();
        for (int cnt = 1; cnt <= MAX_TASKS; cnt++) {
            assertEquals(Integer.toString(cnt),
                    executor.submit(new Task(Integer.toString(cnt), "1111", "user")).get().user());
        }
        executor.shutdown();
    }

    @Test
    public void testNormalThreadPoolExecutor() throws InterruptedException, ExecutionException {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(10);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 500, TimeUnit.MILLISECONDS,
                queue);
        executor.prestartAllCoreThreads();
        for (int cnt = 1; cnt <= MAX_TASKS; cnt++) {
            assertNull(executor.submit(new Task(Integer.toString(cnt), "1111", "user")).get());
        }
        executor.shutdown();
    }

    @Test
    public void testQueueOps() throws InterruptedException, ExecutionException {
        BlockingQueue<String> queue = new SecureBlockingQueue<>(
                new ArrayBlockingQueue<SecureData<String>>(3));
        ExecutorService es = Executors.newFixedThreadPool(3);
        es.submit(new Producer("foo", "1111", "user", queue)).get();
        assertEquals(1, queue.size());
        assertEquals("foo", es.submit(new Consumer(queue)).get());
        es.submit(new Producer("bar", "2222", "user", queue)).get();
        assertEquals("bar", queue.peek());
        assertEquals("bar", queue.element());
        assertEquals(1, queue.size());
        assertEquals("bar", queue.poll());
        assertTrue(queue.isEmpty());
        es.shutdown();
    }

    @Test
    public void testCollectionOps() throws InterruptedException, ExecutionException {
        BlockingQueue<String> queue = new SecureBlockingQueue<>(
                new ArrayBlockingQueue<SecureData<String>>(6));
        for (int i = 1; i <= 3; i++) {
            queue.add("User" + i);
        }
        Iterator<String> it = queue.iterator();
        while (it.hasNext()) {
            assertTrue(it.next().startsWith("User"));
        }
        assertEquals(3, queue.toArray().length);
        List<String> actual = Arrays.asList(queue.toArray(new String[0]));
        assertEquals("User1", actual.iterator().next());
        assertTrue(queue.containsAll(actual));
        queue.addAll(actual);
        assertEquals(6, queue.size());
        queue.retainAll(Arrays.asList(new String[] { "User2" }));
        assertEquals(2, queue.size());
        assertEquals("User2", queue.iterator().next());
        queue.removeAll(actual);
        assertTrue(queue.isEmpty());
        queue.add("hello");
        assertEquals(1, queue.size());
        queue.clear();
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testBlockingQueueOps() throws InterruptedException {
        BlockingQueue<String> queue = new SecureBlockingQueue<>(
                new ArrayBlockingQueue<SecureData<String>>(3));
        queue.offer("foo");
        assertEquals(1, queue.size());
        queue.offer("bar", 500, TimeUnit.MILLISECONDS);
        assertEquals(2, queue.size());
        assertEquals("foo", queue.poll());
        assertTrue(queue.contains("bar"));
        queue.remove("bar");
        assertEquals(3, queue.remainingCapacity());
        queue.addAll(Arrays.asList(new String[] { "foo", "bar", "tom" }));
        assertEquals(3, queue.size());
        assertEquals("foo", queue.poll(500, TimeUnit.MILLISECONDS));
        assertEquals(2, queue.size());
        List<String> drain = new LinkedList<>();
        queue.drainTo(drain);
        assertTrue(queue.isEmpty());
        assertEquals(2, drain.size());
        queue.addAll(Arrays.asList(new String[] { "foo", "bar", "tom" }));
        drain.clear();
        queue.drainTo(drain, 1);
        assertEquals(2, queue.size());
        assertEquals(1, drain.size());
    }

    // Task to run in a ThreadPoolExecutor
    private class Task implements Callable<Authentication> {
        Task(String name, String userId, String role) {
            // Mock that each task has its original authentication context
            AuthenticationManager.instance().set(
                    new AuthenticationBuilder(new ClaimBuilder().setUser(name).setUserId(userId)
                            .addRole(role).build()).build());
        }

        @Override
        public Authentication call() throws Exception {
            return AuthenticationManager.instance().get();
        }
    }

    // Producer sets auth context
    private class Producer implements Callable<String> {
        private final String name;
        private final String userId;
        private final String role;
        private final BlockingQueue<String> queue;

        Producer(String name, String userId, String role, BlockingQueue<String> queue) {
            this.name = name;
            this.userId = userId;
            this.role = role;
            this.queue = queue;
        }

        @Override
        public String call() throws InterruptedException {
            AuthenticationManager.instance().set(
                    new AuthenticationBuilder(new ClaimBuilder().setUser(name).setUserId(userId)
                            .addRole(role).build()).build());
            queue.put(name);
            return name;
        }
    }

    // Consumer gets producer's auth context via data element in queue
    private class Consumer implements Callable<String> {
        private final BlockingQueue<String> queue;

        Consumer(BlockingQueue<String> queue) {
            this.queue = queue;
        }

        @Override
        public String call() {
            queue.remove();
            Authentication auth = AuthenticationManager.instance().get();
            return auth == null ? null : auth.user();
        }
    }
}
