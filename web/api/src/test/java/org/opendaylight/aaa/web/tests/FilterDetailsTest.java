/*
 * Copyright (c) 2018 Lumina Networks, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import javax.servlet.Filter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.opendaylight.aaa.web.FilterDetails;

public class FilterDetailsTest {

    @Test
    public void testDefaultValue() {
        FilterDetails servletDetails = FilterDetails.builder()
                .filter(mock(Filter.class))
                .addUrlPattern("test")
                .addUrlPattern("another")
                .name("custom")
                .putInitParam("key", "value")
                .build();

        assertFalse(servletDetails.getAsyncSupported());
    }

    @Test
    public void testAsyncFalse() {
        FilterDetails servletDetails = FilterDetails.builder()
                .filter(mock(Filter.class))
                .addUrlPattern("test")
                .addUrlPattern("another")
                .name("custom")
                .putInitParam("key", "value")
                .asyncSupported(false)
                .build();

        assertFalse(servletDetails.getAsyncSupported());
    }

    @Test
    public void testAsyncTrue() {
        FilterDetails servletDetails = FilterDetails.builder()
                .filter(mock(Filter.class))
                .addUrlPattern("test")
                .addUrlPattern("another")
                .name("custom")
                .putInitParam("key", "value")
                .asyncSupported(true)
                .build();

        assertTrue(servletDetails.getAsyncSupported());
    }

    @Test
    public void testException() {
        Assert.assertThrows(IllegalStateException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                FilterDetails servletDetails = FilterDetails.builder().build();
            }
        });

    }
}
