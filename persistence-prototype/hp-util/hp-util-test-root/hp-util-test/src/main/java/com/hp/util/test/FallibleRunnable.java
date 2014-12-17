/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import junit.framework.Assert;

/**
 * Decorator to verify in the calling thread whether a {@link Runnable} failed.
 * <p>
 * 
 * <pre>
 *  &#064;Test
 * public void test() throws Exception {
 *     Runnable runnable = new Runnable() {
 *         &#064;Override
 *         public void run() {
 *             ...
 *         }
 *     };
 * 
 *     FallibleRunnable fallibleRunnable = new FallibleRunnable(runnable);
 *     Thread thread = new Thread(fallibleRunnable);
 *     thread.start();
 * 
 *     ...
 * 
 *     thread.join();
 * 
 * 
 *     // Assertions
 *     
 *     fallibleRunnable.verify(); // or "Assert.assertNotNull(fallibleRunnable.getFailure());" if a failure is expected
 *     
 *     ...
 * }
 * 
 * </pre>
 * 
 * @author Fabiel Zuniga
 */
public class FallibleRunnable implements Runnable {

    private volatile Throwable error;
    private Runnable delegate;

    /**
     * Creates a fallible {@link Runnable runnable}.
     * 
     * @param delegate delegate
     */
    public FallibleRunnable(Runnable delegate) {
        if (delegate == null) {
            throw new RuntimeException("delegate cannot be null");
        }
        this.delegate = delegate;
    }

    @Override
    public void run() {
        try {
            this.delegate.run();
        }
        catch (Throwable e) {
            this.error = e;
        }
    }

    /**
     * Gets the error.
     * 
     * @return the error if any has been thrown, {@code null} otherwise.
     */
    public Throwable getFailure() {
        return this.error;
    }

    /**
     * Verifies no {@link Throwable errors} were thrown. If an error was thrown, an
     * {@link AssertionError} is thrown by this method.
     */
    public void verify() {
        if (this.error != null) {
            this.error.printStackTrace();
            Assert.fail(this.error.getMessage());
        }
    }
}
