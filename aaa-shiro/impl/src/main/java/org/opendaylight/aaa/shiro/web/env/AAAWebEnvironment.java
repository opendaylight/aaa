/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 * Copyright (c) 2022 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.shiro.realm.KeystoneAuthRealm;
import org.opendaylight.aaa.shiro.realm.MDSALDynamicAuthorizationFilter;
import org.opendaylight.aaa.shiro.realm.MdsalRealm;
import org.opendaylight.aaa.shiro.realm.MoonRealm;
import org.opendaylight.aaa.shiro.realm.TokenAuthRealm;
import org.opendaylight.aaa.tokenauthrealm.auth.TokenAuthenticators;
import org.opendaylight.aaa.web.servlet.ServletSupport;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends {@code BasicInitEnvironment} to provide the {@link Ini} configuration via a clustered app config,
 * Initialization happens in the context of this class's ClassLoader, with dependencies being injected into their
 * thread-local variables.
 */
public final class AAAWebEnvironment extends IniWebEnvironment {
    private static final Logger LOG = LoggerFactory.getLogger(AAAWebEnvironment.class);

    private AAAWebEnvironment(final Ini ini) {
        setIni(ini);
    }

    public static AAAWebEnvironment create(final ShiroConfiguration shiroConfiguration, final DataBroker dataBroker,
            final ICertificateManager certificateManager, final AuthenticationService authenticationService,
            final TokenAuthenticators tokenAuthenticators, final TokenStore tokenStore,
            final PasswordHashService passwordHashService, final ServletSupport servletSupport) {
        // Turn ShiroConfiguration into an Ini
        final var ini = new Ini();

        final var mainSection = ini.addSection("main");
        for (var main : shiroConfiguration.nonnullMain()) {
            mainSection.put(main.getPairKey(), main.getPairValue());
        }

        final var urlsSection = ini.addSection("urls");
        for (var url : shiroConfiguration.nonnullUrls()) {
            urlsSection.put(url.getPairKey(), url.getPairValue());
        }

        // Create an instance
        final var ret = new AAAWebEnvironment(ini);

        // Configure the instance with all known custom components prepared for loading via their thread locals and
        // clean up afterwards. This needs to happen on our class loader so Shiro's ReflectionBuilder use of
        // Class.forName() is happy.
        ClassLoaderUtils.runWithClassLoader(AAAWebEnvironment.class.getClassLoader(), () -> {
            try (var filterLoad = MDSALDynamicAuthorizationFilter.prepareForLoad(dataBroker);
                 var keyStoneLoad = KeystoneAuthRealm.prepareForLoad(certificateManager, servletSupport);
                 var mdsalLoad = MdsalRealm.prepareForLoad(passwordHashService, dataBroker);
                 var moonLoad = MoonRealm.prepareForLoad(servletSupport);
                 var tokenAuthLoad = TokenAuthRealm.prepareForLoad(authenticationService, tokenAuthenticators,
                     tokenStore)) {
                ret.configure();
            }
        });

        LOG.debug("AAAWebEnvironment created");
        return ret;
    }
}
