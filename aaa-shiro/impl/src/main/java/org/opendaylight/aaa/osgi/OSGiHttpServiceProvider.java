/*
 * Copyright © 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.osgi;

import com.google.common.base.Preconditions;
import java.util.Objects;
import javax.servlet.ServletException;
import org.opendaylight.aaa.shiro.moon.MoonTokenEndpoint;
import org.opendaylight.aaa.shiro.oauth2.OAuth2TokenServlet;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OSGiHttpServiceProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OSGiHttpServiceProvider.class);

    private final HttpService httpService;
    private final String moonEndpointPath;
    private final String oauth2EndpointPath;

    private OSGiHttpServiceProvider(final HttpService httpService, final String moonEndpointPath,
            final String oauth2EndpointPath) {
        this.httpService = Objects.requireNonNull(httpService, "httpService");
        this.moonEndpointPath = Objects.requireNonNull(moonEndpointPath, "moonEndpointPathString");
        this.oauth2EndpointPath = Objects.requireNonNull(oauth2EndpointPath, "oauth2EndpointPathString");
        try {
            registerServletContexts(httpService, moonEndpointPath, oauth2EndpointPath);
        } catch (final ServletException | NamespaceException e) {
            LOG.warn("Could not initialize AAA servlet endpoints", e);
        }
    }

    public static OSGiHttpServiceProvider newInstance(final HttpService httpService, final String moonEndpointPath,
            final String oauth2EndpointPath) {
        return new OSGiHttpServiceProvider(httpService, moonEndpointPath, oauth2EndpointPath);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("AAAShiroProvider Closed");
        this.httpService.unregister(this.moonEndpointPath);
        this.httpService.unregister(this.oauth2EndpointPath);
    }

    private static void registerServletContexts(final HttpService httpService, final String moonEndpointPath,
            final String oauth2EndpointPath) throws ServletException, NamespaceException {
        LOG.info("attempting registration of AAA moon, oauth2 and auth servlets");

        Preconditions.checkNotNull(httpService, "httpService cannot be null");
        httpService.registerServlet(moonEndpointPath, new MoonTokenEndpoint(), null,
                null);
        httpService.registerServlet(oauth2EndpointPath, new OAuth2TokenServlet(), null, null);
    }
}
