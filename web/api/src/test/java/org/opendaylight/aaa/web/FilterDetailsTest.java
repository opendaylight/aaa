/*
 * Copyright (c) 2020 Lumina Networks, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import javax.servlet.Filter;
import org.junit.Test;

public class FilterDetailsTest {
    @Test
    public void testDefaultValue() {
        FilterDetails filterDetails = FilterDetails.builder()
                .filter(mock(Filter.class))
                .addUrlPattern("/test")
                .addUrlPattern("/another")
                .name("custom")
                .putInitParam("key", "value")
                .build();

        assertFalse(filterDetails.getAsyncSupported());
    }

    @Test
    public void testAsyncFalse() {
        FilterDetails filterDetails = FilterDetails.builder()
                .filter(mock(Filter.class))
                .addUrlPattern("/test")
                .addUrlPattern("/another")
                .name("custom")
                .putInitParam("key", "value")
                .asyncSupported(false)
                .build();

        assertFalse(filterDetails.getAsyncSupported());
    }

    @Test
    public void testAsyncTrue() {
        FilterDetails filterDetails = FilterDetails.builder()
                .filter(mock(Filter.class))
                .addUrlPattern("/test")
                .addUrlPattern("/another")
                .name("custom")
                .putInitParam("key", "value")
                .asyncSupported(true)
                .build();

        assertTrue(filterDetails.getAsyncSupported());
    }

    @Test
    public void testException() {
        assertThrows(IllegalStateException.class, () -> FilterDetails.builder().build());
    }
}
