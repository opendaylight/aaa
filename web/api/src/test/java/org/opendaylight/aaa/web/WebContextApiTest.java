/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;
import org.junit.Ignore;
import org.junit.Test;

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
        assertTrue(WebContext.builder().contextPath("test").build().supportsSessions());
        assertEquals("test", WebContext.builder().contextPath("test").supportsSessions(false).build().contextPath());
    }

    @Test
    public void testAddSimpleServlet() {
        WebContext webContext = WebContext.builder().contextPath("test")
                .addServlet(ServletDetails.builder().servlet(mock(Servlet.class)).addUrlPattern("/test").build())
                .build();
        assertThat(webContext.servlets(), hasSize(1));
        ServletDetails firstServletDetail = webContext.servlets().get(0);
        assertThat(firstServletDetail.name(), startsWith("org.mockito.codegen.Servlet$MockitoMock$"));
        assertEquals(Map.of(), firstServletDetail.initParams());
    }

    @Test
    public void testAddFullServlet() {
        WebContext.builder().contextPath("test").addServlet(ServletDetails.builder().servlet(mock(Servlet.class))
                .addUrlPattern("/test").addUrlPattern("/another").name("custom").putInitParam("key", "value").build())
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
                .listeners(), hasSize(1));
    }

    @Test
    public void testContextParam() {
        assertEquals(Map.of("key", "value"),
            WebContext.builder().contextPath("test").putContextParam("key", "value").build().contextParams());
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
}
