/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import org.junit.Assert;
import org.junit.Test;

import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class FallibleRunnableTest {

    @Test
    public void testSucessThread() throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            }
        };

        FallibleRunnable fallibleRunnable = new FallibleRunnable(runnable);
        Thread thread = new Thread(fallibleRunnable);
        thread.start();

        thread.join();

        // This test verifies that no error was produced
        Assert.assertNull(fallibleRunnable.getFailure());
        fallibleRunnable.verify();
    }

    @Test
    public void testFailedThread() throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("Expected error at " + TestUtil.getExecutingMethod());
            }
        };

        final FallibleRunnable fallibleRunnable = new FallibleRunnable(runnable);
        Thread thread = new Thread(fallibleRunnable);
        thread.start();

        thread.join();

        Assert.assertNotNull(fallibleRunnable.getFailure());
        ThrowableTester.testThrowsAny(AssertionError.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                fallibleRunnable.verify();
            }
        });

    }
}
