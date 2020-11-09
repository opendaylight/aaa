/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import javax.inject.Inject;
import javax.servlet.ServletContextListener;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.opendaylight.aaa.shiro.filters.AAAShiroFilter;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextBuilder;
import org.opendaylight.aaa.web.WebContextSecurer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Secures a {@link WebContext} using Shiro.
 *
 * @author Michael Vorburger.ch
 */
@Component(immediate = true, service = WebContextSecurer.class)
public class ShiroWebContextSecurer implements WebContextSecurer {
    private final ServletContextListener shiroEnvironmentLoaderListener;

    @Inject
    @Activate
    public ShiroWebContextSecurer(final @Reference ServletContextListener shiroEnvironmentLoaderListener) {
        this.shiroEnvironmentLoaderListener = shiroEnvironmentLoaderListener;
    }

    @Override
    public void requireAuthentication(final WebContextBuilder webContextBuilder, final boolean asyncSupported,
            final String... urlPatterns) {
        webContextBuilder.addListener(shiroEnvironmentLoaderListener)

                // AAA filter in front of these REST web services as well as for moon endpoints
                .addFilter(FilterDetails.builder()
                        .filter(new AAAShiroFilter())
                        .addUrlPatterns(urlPatterns)
                        .asyncSupported(asyncSupported)
                        .build())

                // CORS filter
                .addFilter(FilterDetails.builder()
                        .filter(new CrossOriginFilter())
                        .addUrlPatterns(urlPatterns)
                        .asyncSupported(asyncSupported)
                        .putInitParam(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*")
                        .putInitParam(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,OPTIONS,DELETE,PUT,HEAD")
                        .putInitParam(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
                            "origin, content-type, accept, authorization")
                        .build());
    }
}
