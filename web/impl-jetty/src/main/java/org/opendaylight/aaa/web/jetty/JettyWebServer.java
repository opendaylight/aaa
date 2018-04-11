/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.jetty;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextRegistration;
import org.opendaylight.aaa.web.WebServer;

/**
 * {@link WebServer} (and {@link WebContext}) implementation based on Jetty.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
// @SuppressWarnings("checkstyle:IllegalCatch") // Jetty LifeCycle start() and stop() throws Exception
public class JettyWebServer implements WebServer {

    public void stop() {
    }

    @Override
    public WebContextRegistration registerWebContext(WebContext webContext) throws ServletException {
        throw new UnsupportedOperationException("TODO Implement!");
    }

    @Override
    public String getBaseURL() {
        throw new UnsupportedOperationException("TODO Implement!");
    }

}
