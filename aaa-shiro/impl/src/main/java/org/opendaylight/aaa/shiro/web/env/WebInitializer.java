/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.opendaylight.aaa.shiro.filters.AAAShiroFilter;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebServer;

/**
 * Initializer for web components.
 *
 * @author Michael Vorburger.ch
 */
public class WebInitializer {

    public WebInitializer(WebServer webServer) {
        // TODO confirm through testing that Jersey & Neutron are fine without sessions
        webServer.registerWebContext(WebContext.builder().contextPath("/auth").hasSessions(false)

            .addServlet(ServletDetails.builder().servlet(new com.sun.jersey.spi.container.servlet.ServletContainer())
                 // TODO test using javax.ws.rs.core.Application.class.getName() instead; NB .core.
                .putInitParam("javax.ws.rs.Application", NeutronNorthboundRSApplication.class.getName())
                .addUrlPattern("/*").build())

             // TODO factor out this common AAA related web context configuration to somewhere shared
             //   instead of likely copy/pasting it from here to WebInitializer classes which will want to do the same
            .putContextParam("shiroEnvironmentClass", KarafIniWebEnvironment.class.getName())
            .addListener(new EnvironmentLoaderListener())
            .addFilter(FilterDetails.builder().filter(new AAAShiroFilter()).addUrlPattern("/*").build())

            .addFilter(FilterDetails.builder().filter(new CrossOriginFilter()).addUrlPattern("/*")
                .putInitParam("allowedOrigins", "*")
                .putInitParam("allowedMethods", "GET,POST,OPTIONS,DELETE,PUT,HEAD")
                .putInitParam("allowedHeaders", "origin, content-type, accept, authorization")
                .build())

            .build());
    }

}
