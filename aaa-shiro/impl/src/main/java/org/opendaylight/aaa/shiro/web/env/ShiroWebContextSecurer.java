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
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextBuilder;
import org.opendaylight.aaa.web.WebContextSecurer;

/**
 * Secures a {@link WebContext} using Shiro.
 *
 * @author Michael Vorburger.ch
 */
public class ShiroWebContextSecurer implements WebContextSecurer {

    private final EnvironmentLoaderListener shiroEnvironmentLoaderListener;

    public ShiroWebContextSecurer(EnvironmentLoaderListener shiroEnvironmentLoaderListener) {
        this.shiroEnvironmentLoaderListener = shiroEnvironmentLoaderListener;
    }

    @Override
    public void requireAuthentication(WebContextBuilder webContextBuilder, String... urlPatterns) {
        webContextBuilder
            .addListener(shiroEnvironmentLoaderListener)

            // AAA filter in front of these REST web services as well as for moon endpoints
            .addFilter(FilterDetails.builder().filter(new AAAShiroFilter()).addUrlPattern(urlPatterns).build())

            // CORS filter
            .addFilter(FilterDetails.builder().filter(new CrossOriginFilter()).addUrlPattern(urlPatterns)
               .putInitParam("allowedOrigins", "*")
               .putInitParam("allowedMethods", "GET,POST,OPTIONS,DELETE,PUT,HEAD")
               .putInitParam("allowedHeaders", "origin, content-type, accept, authorization")
               .build());
    }

}
