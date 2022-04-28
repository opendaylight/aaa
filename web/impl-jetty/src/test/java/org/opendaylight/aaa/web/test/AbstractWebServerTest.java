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
import static org.junit.Assert.assertTrue;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import org.junit.Test;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextBuilder;
import org.opendaylight.aaa.web.WebContextRegistration;
import org.opendaylight.aaa.web.WebServer;

/**
 * Test of {@link WebServer} API.
 *
 * @author Michael Vorburger.ch
 */
public abstract class AbstractWebServerTest {

    /** Returns the WebServer to test.  This should not return a new one on each call, but a fresh one per test. */
    protected abstract WebServer getWebServer();

    @Test
    public void testAddAfterStart() throws ServletException, IOException {
        WebContextBuilder webContextBuilder = WebContext.builder().contextPath("/test1");
        webContextBuilder.addServlet(
                ServletDetails.builder().addUrlPattern("/*").name("Test").servlet(new TestServlet()).build());
        WebContextRegistration webContextRegistration = getWebServer().registerWebContext(webContextBuilder.build());
        checkTestServlet(getWebServer().getBaseURL() + "/test1");
        webContextRegistration.close();
    }

    @Test
    public void testAddAfterStartWithoutSlashOnContext() throws ServletException, IOException {
        // NB subtle difference to previous test: contextPath("test1") instead of /test1 with slash!
        WebContextBuilder webContextBuilder = WebContext.builder().contextPath("test1");
        webContextBuilder.addServlet(
                ServletDetails.builder().addUrlPattern("/*").name("Test").servlet(new TestServlet()).build());
        WebContextRegistration webContextRegistration = getWebServer().registerWebContext(webContextBuilder.build());
        checkTestServlet(getWebServer().getBaseURL() + "/test1");
        webContextRegistration.close();
    }

    @Test
    public void testAddFilter() throws Exception {
        TestFilter testFilter = new TestFilter();
        WebContextBuilder webContextBuilder = WebContext.builder().contextPath("/testingFilters");
        webContextBuilder
            .putContextParam("testParam1", "avalue")
            .addFilter(FilterDetails.builder().addUrlPattern("/*").name("Test").filter(testFilter).build());
        WebContextRegistration webContextRegistration = getWebServer().registerWebContext(webContextBuilder.build());
        assertTrue(testFilter.isInitialized);
        webContextRegistration.close();
    }

    @Test
    public void testRegisterListener() throws Exception {
        WebContextBuilder webContextBuilder = WebContext.builder().contextPath("/testingListener");
        TestListener testListener = new TestListener();
        webContextBuilder.addListener(testListener);
        assertFalse(testListener.isInitialized);
        WebContextRegistration webContextRegistration = getWebServer().registerWebContext(webContextBuilder.build());
        assertTrue(testListener.isInitialized);
        webContextRegistration.close();
        assertFalse(testListener.isInitialized);
    }

    static void checkTestServlet(String urlPrefix) throws IOException {
        try (InputStream inputStream = new URL(urlPrefix + "/something").openConnection().getInputStream()) {
            // The hard-coded ASCII here is strictly speaking wrong of course
            // (should interpret header from reply), but good enough for a test.
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.US_ASCII)) {
                assertEquals("hello, world\r\n", CharStreams.toString(reader));
            }
        }
    }
}
