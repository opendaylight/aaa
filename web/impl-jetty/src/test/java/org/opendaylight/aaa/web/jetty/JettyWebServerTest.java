/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.jetty;

import org.junit.After;
import org.junit.Before;
import org.opendaylight.aaa.web.WebServer;
import org.opendaylight.aaa.web.test.AbstractWebServerTest;

/**
 * Tests {@link JettyWebServer}.
 *
 * @author Michael Vorburger.ch
 */
public class JettyWebServerTest extends AbstractWebServerTest {

    private JettyWebServer webServer;

    @Before
    @SuppressWarnings("checkstyle:IllegalThrows")
    public void beforeTest() throws Throwable {
        webServer = new JettyWebServer();
        webServer.start();
    }

    @Override
    protected WebServer getWebServer() {
        return webServer;
    }

    @After
    public void afterTest() throws Exception {
        webServer.stop();
    }
}
