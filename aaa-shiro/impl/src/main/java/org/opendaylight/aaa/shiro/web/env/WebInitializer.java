/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import org.opendaylight.aaa.api.ClaimCache;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.filterchain.configuration.CustomFilterAdapterConfiguration;
import org.opendaylight.aaa.filterchain.filters.CustomFilterAdapter;
import org.opendaylight.aaa.shiro.idm.IdmLightApplication;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.ServletDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextSecurer;
import org.opendaylight.aaa.web.WebServer;
import org.opendaylight.aaa.web.servlet.ServletSupport;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Initializer for web components.
 * This class is the equivalent of a declarative web.xml,
 * and is not OSGi specific; it can also be used e.g. in standalone
 * Java environments, such as tests.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
@Component(immediate = true)
public class WebInitializer {
    private final Registration registraton;

    @Inject
    @Activate
    public WebInitializer(final @Reference WebServer webServer, final @Reference ClaimCache claimCache,
            final @Reference IIDMStore iidMStore, final @Reference WebContextSecurer webContextSecurer,
            final @Reference ServletSupport servletSupport,
            final @Reference CustomFilterAdapterConfiguration customFilterAdapterConfig) throws ServletException {
        final var webContextBuilder = WebContext.builder()
            .name("OpenDaylight IDM realm management")
            .contextPath("/auth")
            .supportsSessions(true)

            .addServlet(ServletDetails.builder()
                .servlet(servletSupport.createHttpServletBuilder(new IdmLightApplication(iidMStore, claimCache))
                    .build())
                .addUrlPattern("/*")
                .build())

            // Allows user to add javax.servlet.Filter(s) in front of REST services
            .addFilter(FilterDetails.builder()
                .filter(new CustomFilterAdapter(customFilterAdapterConfig))
                .addUrlPattern("/*")
                .build());

        webContextSecurer.requireAuthentication(webContextBuilder, "/*", "/moon/*");

        registraton = webServer.registerWebContext(webContextBuilder.build());
    }

    @PreDestroy
    public void close() {
        registraton.close();
    }
}
