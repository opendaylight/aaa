/*
 * Copyright (c) 2020 Lumina Networks, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import javax.servlet.Servlet;
import org.junit.Test;

public class ServletDetailsTest {
    @Test
    public void testDefaultValue() {
        final var servletDetails = ServletDetails.builder()
            .servlet(mock(Servlet.class))
            .addUrlPattern("/test")
            .addUrlPattern("/another")
            .name("custom")
            .putInitParam("key", "value")
            .build();

        assertFalse(servletDetails.asyncSupported());
    }

    @Test
    public void testAsyncFalse() {
        final var servletDetails = ServletDetails.builder()
            .servlet(mock(Servlet.class))
            .addUrlPattern("/test")
            .addUrlPattern("/another")
            .name("custom")
            .putInitParam("key", "value")
            .asyncSupported(false)
            .build();

        assertFalse(servletDetails.asyncSupported());
    }

    @Test
    public void testAsyncTrue() {
        final var servletDetails = ServletDetails.builder()
            .servlet(mock(Servlet.class))
            .addUrlPattern("/test")
            .addUrlPattern("/another")
            .name("custom")
            .putInitParam("key", "value")
            .asyncSupported(true)
            .build();

        assertTrue(servletDetails.asyncSupported());
    }

    @Test
    public void testEmptyBuilderException() {
        final var builder = ServletDetails.builder();
        final var ex = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("No servlet specified", ex.getMessage());
    }

    @Test
    public void testBadServletWithoutAnyURL() {
        final var builder = ServletDetails.builder().servlet(mock(Servlet.class));
        final var ex = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("No urlPattern specified", ex.getMessage());
    }
}
