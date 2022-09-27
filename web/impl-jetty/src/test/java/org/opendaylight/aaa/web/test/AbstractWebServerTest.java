/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import org.junit.Test;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebServer;

/**
 * Test of {@link WebServer} API.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractWebServerTest {
    // Returns the WebServer to test.  This should not return a new one on each call, but a fresh one per test.
    protected abstract WebServer getWebServer();

    @Test
    public void testAddAfterStart() throws ServletException, IOException {
        var webContext = WebContext.builder()
            .contextPath("/test1")
            .addServlet(ServletDetails.builder().addUrlPattern("/*").name("Test").servlet(new TestServlet()).build())
            .build();
        try (var webContextRegistration = getWebServer().registerWebContext(webContext)) {
            checkTestServlet(getWebServer().getBaseURL() + "/test1");
        }
    }

    @Test
    public void testAddAfterStartWithoutSlashOnContext() throws ServletException, IOException {
        // NB subtle difference to previous test: contextPath("test1") instead of /test1 with slash!
        var webContext = WebContext.builder()
            .contextPath("test1")
            .addServlet(ServletDetails.builder().addUrlPattern("/*").name("Test").servlet(new TestServlet()).build())
            .build();
        try (var webContextRegistration = getWebServer().registerWebContext(webContext)) {
            checkTestServlet(getWebServer().getBaseURL() + "/test1");
        }
    }

    @Test
    public void testAddAfterStartWithoutSlashOnServlet() throws ServletException, IOException {
        // NB subtle difference to testAddAfterStart() test: addUrlPattern("*") instead of /* with slash!
        assertThrows(IllegalArgumentException.class, () -> WebContext.builder()
                .contextPath("/test1")
                .addServlet(ServletDetails.builder().addUrlPattern("*").name("Test").servlet(new TestServlet()).build())
                .build());
    }

    @Test
    public void testAddFilter() throws Exception {
        var testFilter = new TestFilter();
        var webContext = WebContext.builder()
            .contextPath("/testingFilters")
            .putContextParam("testParam1", "avalue")
            .addFilter(FilterDetails.builder().addUrlPattern("/*").name("Test").filter(testFilter).build())
            .build();
        try (var webContextRegistration = getWebServer().registerWebContext(webContext)) {
            assertTrue(testFilter.isInitialized);
        }
    }

    @Test
    public void testAddFilterWithoutSlash() throws Exception {
        // NB subtle difference to previous test: addUrlPattern("*") instead of /* with slash!
        var testFilter = new TestFilter();
        var webContext = WebContext.builder()
                .contextPath("/testingFilters")
                .putContextParam("testParam1", "avalue")
                .addFilter(FilterDetails.builder().addUrlPattern("*").name("Test").filter(testFilter).build())
                .build();
        try (var webContextRegistration = getWebServer().registerWebContext(webContext)) {
            assertTrue(testFilter.isInitialized);
        }
    }

    @Test
    public void testRegisterListener() throws Exception {
        var testListener = new TestListener();
        var webContext = WebContext.builder()
            .contextPath("/testingListener")
            .addListener(testListener)
            .build();
        assertFalse(testListener.isInitialized);
        try (var webContextRegistration = getWebServer().registerWebContext(webContext)) {
            assertTrue(testListener.isInitialized);
        }
        assertFalse(testListener.isInitialized);
    }

    static void checkTestServlet(final String urlPrefix) throws IOException {
        try (var inputStream = new URL(urlPrefix + "/something").openConnection().getInputStream()) {
            // The hard-coded ASCII here is strictly speaking wrong of course
            // (should interpret header from reply), but good enough for a test.
            try (var reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII)) {
                assertEquals("hello, world\r\n", CharStreams.toString(reader));
            }
        }
    }
}
