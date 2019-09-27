/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.tests;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextBuilder;
import org.opendaylight.aaa.web.WebServer;

/**
 * Tests for Web Server {@link WebContext} API. These tests don't test an
 * {@link WebServer} implementation; the purpose is just to compile time check
 * against the API signatures, notably incl. their generated code.
 *
 * @author Michael Vorburger.ch
 */
public class WebContextApiTest {

    @Test
    public void testEmptyBuilder() {
        final WebContextBuilder builder = WebContext.builder();
        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    public void testMinimalBuilder() {
        assertThat(WebContext.builder().contextPath("test").build().supportsSessions()).isTrue();
        assertThat(WebContext.builder().contextPath("test").supportsSessions(false).build().contextPath())
                .isEqualTo("test");
    }

    @Test
    public void testAddSimpleServlet() {
        WebContext webContext = WebContext.builder().contextPath("test")
                .addServlet(ServletDetails.builder().servlet(mock(Servlet.class)).addUrlPattern("test").build())
                .build();
        assertThat(webContext.servlets()).hasSize(1);
        ServletDetails firstServletDetail = webContext.servlets().get(0);
        assertThat(firstServletDetail.name())
                .startsWith("org.mockito.codegen.Servlet$MockitoMock$");
        assertThat(firstServletDetail.initParams()).isEmpty();
    }

    @Test
    public void testAddFullServlet() {
        WebContext.builder().contextPath("test").addServlet(ServletDetails.builder().servlet(mock(Servlet.class))
                .addUrlPattern("test").addUrlPattern("another").name("custom").putInitParam("key", "value").build())
                .build();
    }

    @Test
    public void testAddFilter() {
        WebContext.builder().contextPath("test")
            .addFilter(FilterDetails.builder().filter(mock(Filter.class)).addUrlPattern("test").build()).build();
    }

    @Test
    public void testAddListener() {
        assertThat(WebContext.builder().contextPath("test").addListener(mock(ServletContextListener.class)).build()
                .listeners()).isNotEmpty();
    }

    @Test
    public void testContextParam() {
        assertThat(WebContext.builder().contextPath("test").putContextParam("key", "value").build().contextParams())
                .containsExactly("key", "value").inOrder();
    }

    @Test
    @Ignore
    public void testBadContextPath() {
        // FIXME: this is completely broken usage -- which call is expected to raise the exception?!
        assertThrows(IllegalArgumentException.class, () -> WebContext.builder().contextPath("test/sub").build());
        assertThrows(IllegalArgumentException.class, () -> WebContext.builder().contextPath("test space").build());
        assertThrows(IllegalArgumentException.class, () -> WebContext.builder().contextPath("/test").build());
        assertThrows(IllegalArgumentException.class, () -> WebContext.builder().contextPath("test/").build());
    }

    @Test
    public void testBadServletWithoutAnyURL() {
        final WebContextBuilder builder = WebContext.builder().contextPath("test")
                .addServlet(ServletDetails.builder().servlet(mock(Servlet.class)).build());
        assertThrows(IllegalArgumentException.class, () -> builder.build());
    }

    @Test
    public void testBadFilterWithoutAnyURL() {
        final WebContextBuilder builder = WebContext.builder().contextPath("test")
                .addFilter(FilterDetails.builder().filter(mock(Filter.class)).build());
        assertThrows(IllegalArgumentException.class, () -> builder.build());
    }

    private static <T extends RuntimeException> void assertThrows(final Class<T> clazz, final Runnable runnable) {
        try {
            runnable.run();
            fail("Expected " + clazz.getName());
        } catch (RuntimeException e) {
            assertThat(e).isInstanceOf(clazz);
        }
    }
}
