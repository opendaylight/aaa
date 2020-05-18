/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.testutils;

import com.google.inject.AbstractModule;
import org.opendaylight.aaa.web.WebContextSecurer;
import org.opendaylight.aaa.web.WebServer;
import org.opendaylight.aaa.web.jetty.JettyWebServer;
import org.opendaylight.aaa.web.servlet.ServletSupport;
import org.opendaylight.aaa.web.servlet.jersey2.JerseyServletSupport;

/**
 * Guice Module wiring up a test web server running HTTP on a random port, and registering a NOOP WebContextSecurer.
 *
 * @author Michael Vorburger.ch
 */
public class WebTestModule extends AbstractModule {

    @Override
    protected void configure() {
        // NB: We use the constructor without the port argument
        bind(WebServer.class).toInstance(new JettyWebServer());

        // JAX-RS
        bind(ServletSupport.class).to(JerseyServletSupport.class);

        // NB: This is a NOOP WebContextSecurer
        // TODO: LATER offer one with a fixed uid/pwd for HTTP BASIC, using Jetty's Filter
        bind(WebContextSecurer.class).toInstance((webContextBuilder, asyncSupported, urlPatterns) -> { });
    }
}
