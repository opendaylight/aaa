/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.opendaylight.aaa.TokenProvider;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.web.servlet.ServletSupport;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, service = WebEnvironment.class)
public final class OSGiAAAWebEnvironment implements WebEnvironment {
    private final WebEnvironment delegate;

    @Inject
    @Activate
    public OSGiAAAWebEnvironment(@Reference final OSGiAAAShiroConfig shiroConfig,
            @Reference final DataBroker dataBroker, @Reference final ICertificateManager certificateManager,
            @Reference final AuthenticationService authenticationService, @Reference final TokenProvider tokenProvider,
            @Reference final PasswordHashService passwordHashService, @Reference final ServletSupport servletSupport) {
        delegate = AAAWebEnvironment.create(shiroConfig, dataBroker, certificateManager, authenticationService,
                tokenProvider.getTokenAuthenticators(), tokenProvider.getTokenStore(), passwordHashService,
                servletSupport);
    }

    @Override
    public SecurityManager getSecurityManager() {
        return delegate.getSecurityManager();
    }

    @Override
    public FilterChainResolver getFilterChainResolver() {
        return delegate.getFilterChainResolver();
    }

    @Override
    public ServletContext getServletContext() {
        return delegate.getServletContext();
    }

    @Override
    public WebSecurityManager getWebSecurityManager() {
        return delegate.getWebSecurityManager();
    }
}
