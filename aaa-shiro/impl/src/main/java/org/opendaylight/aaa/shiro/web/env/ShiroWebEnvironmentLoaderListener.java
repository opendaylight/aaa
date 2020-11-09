/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;
import org.opendaylight.aaa.TokenProvider;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.shiro.tokenauthrealm.auth.TokenAuthenticators;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes shiro components for a web environment.
 *
 * @author Thomas Pantelis
 */
@Component(immediate = true, service = ServletContextListener.class)
public class ShiroWebEnvironmentLoaderListener extends EnvironmentLoaderListener {
    private static final Logger LOG = LoggerFactory.getLogger(ShiroWebEnvironmentLoaderListener.class);

    private final ShiroConfiguration shiroConfiguration;
    private final DataBroker dataBroker;
    private final ICertificateManager certificateManager;
    private final AuthenticationService authenticationService;
    private final TokenAuthenticators tokenAuthenticators;
    private final TokenStore tokenStore;
    private final PasswordHashService passwordHashService;

    @Inject
    @Activate
    public ShiroWebEnvironmentLoaderListener(final @Reference ShiroConfiguration shiroConfiguration,
                                             final @Reference DataBroker dataBroker,
                                             final @Reference ICertificateManager certificateManager,
                                             final @Reference AuthenticationService authenticationService,
                                             final @Reference TokenProvider tokenProvider,
                                             final @Reference PasswordHashService passwordHashService) {
        this.shiroConfiguration = shiroConfiguration;
        this.dataBroker = dataBroker;
        this.certificateManager = certificateManager;
        this.authenticationService = authenticationService;
        this.tokenAuthenticators = tokenProvider.getTokenAuthenticators();
        this.tokenStore = tokenProvider.getTokenStore();
        this.passwordHashService = passwordHashService;
        LOG.debug("ShiroWebEnvironmentLoaderListenerImpl created");
    }

    @Override
    protected WebEnvironment determineWebEnvironment(final ServletContext servletContext) {
        return new AAAIniWebEnvironment(shiroConfiguration, dataBroker, certificateManager, authenticationService,
            tokenAuthenticators, tokenStore, passwordHashService);
    }
}
