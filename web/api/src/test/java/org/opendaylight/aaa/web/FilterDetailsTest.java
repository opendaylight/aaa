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

import javax.servlet.Filter;
import org.junit.Test;

public class FilterDetailsTest {
    @Test
    public void testDefaultValue() {
        var filterDetails = FilterDetails.builder()
            .filter(mock(Filter.class))
            .addUrlPattern("/test")
            .addUrlPattern("/another")
            .name("custom")
            .putInitParam("key", "value")
            .build();

        assertFalse(filterDetails.asyncSupported());
    }

    @Test
    public void testAsyncFalse() {
        var filterDetails = FilterDetails.builder()
                .filter(mock(Filter.class))
                .addUrlPattern("/test")
                .addUrlPattern("/another")
                .name("custom")
                .putInitParam("key", "value")
                .asyncSupported(false)
                .build();

        assertFalse(filterDetails.asyncSupported());
    }

    @Test
    public void testAsyncTrue() {
        var filterDetails = FilterDetails.builder()
            .filter(mock(Filter.class))
            .addUrlPattern("/test")
            .addUrlPattern("/another")
            .name("custom")
            .putInitParam("key", "value")
            .asyncSupported(true)
            .build();

        assertTrue(filterDetails.asyncSupported());
    }

    @Test
    public void testEmptyBuilderException() {
        final var builder = FilterDetails.builder();
        final var ex = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("No filter specified", ex.getMessage());
    }

    @Test
    public void testBadFilterWithoutAnyURL() {
        final var builder = FilterDetails.builder().filter(mock(Filter.class));
        final var ex = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("No urlPattern specified", ex.getMessage());
    }

    @Test
    public void testNotPrefixNorSuffixPatternException() {
        final var builder = FilterDetails.builder();
        final var ex = assertThrows(IllegalArgumentException.class, () -> builder.addUrlPattern("test"));
        assertEquals("Spec 'test' is neither prefix-based nor suffix-based", ex.getMessage());
    }

    @Test
    public void testIllegalPrefixPatternException() {
        final var builder = FilterDetails.builder();
        final var ex = assertThrows(IllegalArgumentException.class, () -> builder.addUrlPattern("/*test"));
        assertEquals("Prefix-based spec '/*test' with a '*' at offset 1", ex.getMessage());
    }

    @Test
    public void testIllegalSuffixPatternException() {
        final var builder = FilterDetails.builder();
        final var ex = assertThrows(IllegalArgumentException.class, () -> builder.addUrlPattern("*./test"));
        assertEquals("Spec '*./test' is neither prefix-based nor suffix-based", ex.getMessage());
    }
}
