/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import javax.servlet.ServletException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.filterchain.filters.CustomFilterAdapter;
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

    public WebInitializer(WebServer webServer, IIDMStore iidMStore) throws ServletException {

        this.registraton = webServer.registerWebContext(WebContext.builder().contextPath("auth").supportsSessions(true)

            .addServlet(ServletDetails.builder().servlet(new com.sun.jersey.spi.container.servlet.ServletContainer(
                    new IdmLightApplication(iidMStore)))
                .putInitParam("com.sun.jersey.api.json.POJOMappingFeature", "true")
                .putInitParam("jersey.config.server.provider.packages", "org.opendaylight.aaa.impl.provider")
                .addUrlPattern("/*").build())

            // Allows user to add javax.servlet.Filter(s) in front of REST services
            .addFilter(FilterDetails.builder().filter(new CustomFilterAdapter()).addUrlPattern("/*").build())

            .addUrlPatternsRequiringAuthentication("/*", "/moon/*")

            .build());
    }

    public void close() {
        registraton.close();
    }

}
