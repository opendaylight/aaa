/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.testutils.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextBuilder;
import org.opendaylight.aaa.web.WebContextRegistration;
import org.opendaylight.aaa.web.WebServer;
import org.opendaylight.aaa.web.testutils.TestWebClient;
import org.opendaylight.aaa.web.testutils.WebTestModule;
import org.opendaylight.infrautils.inject.guice.testutils.AnnotationsModule;
import org.opendaylight.infrautils.inject.guice.testutils.GuiceRule;

/**
 * Test the {@link WebTestModule}.
 *
 * @author Michael Vorburger.ch
 */
public class WebTestModuleTest {

    public @Rule GuiceRule guice = new GuiceRule(WebTestModule.class, AnnotationsModule.class);

    @Inject WebServer webServer;
    @Inject TestWebClient webClient;

    @Test
    public void testServlet() throws ServletException, IOException, InterruptedException, URISyntaxException {
        WebContextBuilder webContextBuilder = WebContext.builder().contextPath("/test1");
        webContextBuilder.addServlet(
                ServletDetails.builder().addUrlPattern("/hello").name("Test").servlet(new TestServlet()).build());
        try (WebContextRegistration webContextRegistration = webServer.registerWebContext(webContextBuilder.build())) {
            assertEquals("hello, world", webClient.request("GET", "test1/hello").body());
            assertEquals("hello, world", webClient.request("GET", "/test1/hello").body());
        }
    }

    static class TestServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(final HttpServletRequest req, final HttpServletResponse response) throws IOException {
            response.getOutputStream().print("hello, world");
        }
    }
}
