/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;

public class AuthenticationManagerTest {
    private static final AuthenticationService as = AuthenticationManager
            .instance();

    @Before
    public void tearDown() {
        as.set(null);
    }

    @Test
    public void testSameThread() {
        assertEquals(setAuth(), as.get());
    }

    @Test
    public void testSpawnedThread() throws InterruptedException,
            ExecutionException {
        Authentication auth = setAuth();
        Future<Authentication> f = Executors.newSingleThreadExecutor().submit(
                new Worker());
        assertEquals(auth, f.get());
    }

    @Test
    public void testSpawnedThreadPool() throws InterruptedException,
            ExecutionException {
        Authentication auth = setAuth();
        List<Future<Authentication>> fs = Executors.newFixedThreadPool(2)
                .invokeAll(Arrays.asList(new Worker(), new Worker()));
        for (Future<Authentication> f : fs)
            assertEquals(auth, f.get());
    }

    @Test
    public void testContextPassing() throws InterruptedException,
            ExecutionException {
        Broker broker = new Broker();
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        threadPool.execute(new Consumer("1", broker));
        threadPool.execute(new Consumer("2", broker));
        threadPool.submit(new Producer(broker)).get();
        threadPool.shutdown();
    }

    // Data queue
    private class Broker {
        BlockingQueue<SecureInteger> queue = new ArrayBlockingQueue<SecureInteger>(10);
        volatile boolean continueProducing = true;

        public void put(Integer data) throws InterruptedException {
            // Set current security context into data
            queue.put(new SecureInteger(data, as.get()));
        }

        public Integer get() throws InterruptedException {
            SecureInteger i = queue.poll(1, TimeUnit.SECONDS);
            // Set current security context from data
            as.set(i.auth);
            return i.data;
        }

        // Injected security data
        final class SecureInteger {
            Integer data;
            Authentication auth;
            SecureInteger(Integer data, Authentication auth) {
                this.data = data;
                this.auth = auth;
            }
        }
    }

    // Producer security context induced
    private class Producer implements Runnable {
        private final Broker broker;

        public Producer(Broker broker) {
            this.broker = broker;
        }

        @Override
        public void run() {
            try {
                for (Integer i = 1; i < 5 + 1; ++i) {
                    System.out.println("Producer produced: " + i);
                    // Each data produced has a different security context!
                    setAuth(i.toString());
                    Thread.sleep(100);
                    broker.put(i);
                }
                broker.continueProducing = false;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Security context passed to consumer
    private class Consumer implements Runnable {
        private final String name;
        private final Broker broker;

        public Consumer(String name, Broker broker) {
            this.name = name;
            this.broker = broker;
        }

        @Override
        public void run() {
            try {
                Integer data = broker.get();
                while (broker.continueProducing || data != null) {
                    System.out.println("Consumer " + this.name + " consumes: "
                            + data + " " + as.get());
                    data = broker.get();
                    assertEquals(data.toString(), as.get().userName());
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class Worker implements Callable<Authentication> {
        @Override
        public Authentication call() throws Exception {
            return as.get();
        }
    }

    private Authentication setAuth(String name) {
        Authentication auth = new AuthenticationBuilder().setUserName(name)
                .addRole("admin").addRole("guest").build();
        as.set(auth);
        return auth;
    }

    private Authentication setAuth() {
        return setAuth("Bob");
    }

}
