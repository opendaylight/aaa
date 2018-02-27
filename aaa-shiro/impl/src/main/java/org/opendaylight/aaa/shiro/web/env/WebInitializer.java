/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import javax.servlet.ServletException;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.opendaylight.aaa.filterchain.filters.CustomFilterAdapter;
import org.opendaylight.aaa.shiro.filters.AAAShiroFilter;
import org.opendaylight.aaa.shiro.idm.IdmLightApplication;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextRegistration;
import org.opendaylight.aaa.web.WebServer;

/**
 * Initializer for web components.
 * This class is the equivalent of a declarative web.xml,
 * and is not OSGi specific; it can also be used e.g. in standalone
 * Java environments, such as tests.
 *
 * @author Michael Vorburger.ch
 */
public class WebInitializer {

    private final WebContextRegistration registraton;

    public WebInitializer(WebServer webServer) throws ServletException {
        this.registraton = webServer.registerWebContext(WebContext.builder().contextPath("auth").supportsSessions(true)

            .addServlet(ServletDetails.builder().servlet(new com.sun.jersey.spi.container.servlet.ServletContainer())
                 // TODO test using javax.ws.rs.core.Application.class.getName() instead; NB .core.
                 //   or, even much more better, use new ServletContainer(new IdmLightApplication()) ...
                 //   as that is, ultimately, one of the main reasons for doing it like this!
                .putInitParam("javax.ws.rs.Application", IdmLightApplication.class.getName())
                .putInitParam("com.sun.jersey.api.json.POJOMappingFeature", "true")
                .putInitParam("jersey.config.server.provider.packages", "org.opendaylight.aaa.impl.provider")
                .addUrlPattern("/*").build())

             // TODO factor out this common AAA related web context configuration to somewhere shared instead of
             //   copy/pasting it from here to WebInitializer classes in other project, which will want to do the same.

             //  Shiro initialization
            .addListener(new KarafIniWebEnvironmentLoaderListener())
             // Allows user to add javax.servlet.Filter(s) in front of REST services
            .addFilter(FilterDetails.builder().filter(new CustomFilterAdapter()).addUrlPattern("/*").build())
             // AAA filter in front of these REST web services as well as for moon endpoints
            .addFilter(FilterDetails.builder().filter(new AAAShiroFilter()).addUrlPattern("/*", "/moon/*").build())
             // CORS filter
            .addFilter(FilterDetails.builder().filter(new CrossOriginFilter()).addUrlPattern("/*")
                .putInitParam("allowedOrigins", "*")
                .putInitParam("allowedMethods", "GET,POST,OPTIONS,DELETE,PUT,HEAD")
                .putInitParam("allowedHeaders", "origin, content-type, accept, authorization")
                .build())

            .build());
    }

    public void close() {
        registraton.close();
    }

}
