/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import javax.servlet.ServletException;
import org.opendaylight.aaa.api.ClaimCache;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterConfiguration;
import org.opendaylight.aaa.filterchain.filters.CustomFilterAdapter;
import org.opendaylight.aaa.shiro.idm.IdmLightApplication;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextBuilder;
import org.opendaylight.aaa.web.WebContextRegistration;
import org.opendaylight.aaa.web.WebContextSecurer;
import org.opendaylight.aaa.web.WebServer;
import org.opendaylight.aaa.web.servlet.ServletSupport;

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

    public WebInitializer(final WebServer webServer, final ClaimCache claimCache, final IIDMStore iidMStore,
            final WebContextSecurer webContextSecurer,
            final CustomFilterAdapterConfiguration customFilterAdapterConfig,
            final ServletSupport builderFactory) throws ServletException {

        WebContextBuilder webContextBuilder = WebContext.builder()
                .contextPath("auth")
                .supportsSessions(true)
                .addServlet(ServletDetails.builder().servlet(builderFactory.createHttpServletBuilder(
                    new IdmLightApplication(iidMStore, claimCache)).build())
                .putInitParam("com.sun.jersey.api.json.POJOMappingFeature", "true")
                .putInitParam("jersey.config.server.provider.packages", "org.opendaylight.aaa.impl.provider")
                .addUrlPattern("/*").build())

            // Allows user to add javax.servlet.Filter(s) in front of REST services
            .addFilter(FilterDetails.builder().filter(new CustomFilterAdapter(customFilterAdapterConfig))
                    .addUrlPattern("/*").build());

        webContextSecurer.requireAuthentication(webContextBuilder, "/*", "/moon/*");

        this.registraton = webServer.registerWebContext(webContextBuilder.build());
    }

    public void close() {
        registraton.close();
    }

}
