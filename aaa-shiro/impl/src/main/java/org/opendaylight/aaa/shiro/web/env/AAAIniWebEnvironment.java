/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import static java.util.Objects.requireNonNull;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.shiro.configuration.Main;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.shiro.configuration.Urls;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends <code>IniWebEnvironment</code> to provide the Ini configuration via a clustered app config,
 * and sets the TCCL (x2) so that loading of classes by name (from aaa-app-config.xml) works even with
 * ShiroWebContextSecurer.
 *
 * @author Ryan Goulding
 * @author Thomas Pantelis
 * @author Michael Vorburger - use of TCCL for ShiroWebContextSecurer
 */
public final class AAAIniWebEnvironment extends IniWebEnvironment {
    private static final Logger LOG = LoggerFactory.getLogger(AAAIniWebEnvironment.class);

    private static final String MAIN_SECTION_HEADER = "main";
    private static final String URLS_SECTION_HEADER = "urls";

    private final ShiroConfiguration shiroConfiguration;
    private final DataBroker dataBroker;
    private final ICertificateManager certificateManager;
    private final AuthenticationService authenticationService;
    private final TokenAuthenticators tokenAuthenticators;
    private final TokenStore tokenStore;
    private final PasswordHashService passwordHashService;
    private final ServletSupport servletSupport;

    public AAAIniWebEnvironment(final ShiroConfiguration shiroConfiguration, final DataBroker dataBroker,
            final ICertificateManager certificateManager, final AuthenticationService authenticationService,
            final TokenAuthenticators tokenAuthenticators, final TokenStore tokenStore,
            final PasswordHashService passwordHashService, final ServletSupport servletSupport) {
        this.shiroConfiguration = requireNonNull(shiroConfiguration);
        this.dataBroker = requireNonNull(dataBroker);
        this.certificateManager = requireNonNull(certificateManager);
        this.authenticationService = requireNonNull(authenticationService);
        this.tokenAuthenticators = requireNonNull(tokenAuthenticators);
        this.tokenStore = requireNonNull(tokenStore);
        this.passwordHashService = requireNonNull(passwordHashService);
        this.servletSupport = requireNonNull(servletSupport);
        LOG.debug("AAAIniWebEnvironment created");
    }

    static Ini createIniFromClusteredAppConfig(final ShiroConfiguration shiroConfiguration) {
        final Ini ini = new Ini();

        final Ini.Section mainSection = ini.addSection(MAIN_SECTION_HEADER);
        for (final Main main : shiroConfiguration.nonnullMain()) {
            mainSection.put(main.getPairKey(), main.getPairValue());
        }

        final Ini.Section urlsSection = ini.addSection(URLS_SECTION_HEADER);
        for (final Urls url : shiroConfiguration.nonnullUrls()) {
            urlsSection.put(url.getPairKey(), url.getPairValue());
        }

        final Factory<SecurityManager> factory = new IniSecurityManagerFactory(ini);
        final SecurityManager securityManager = ClassLoaderUtils.getWithClassLoader(
                AAAIniWebEnvironment.class.getClassLoader(), factory::getInstance);
        // FIXME: do not set global security manager
        SecurityUtils.setSecurityManager(securityManager);

        return ini;
    }

    @Override
    public void init() {
        try (
            var filterLoad = MDSALDynamicAuthorizationFilter.prepareForLoad(dataBroker);
            var keyStoneLoad = KeystoneAuthRealm.prepareForLoad(certificateManager, servletSupport);
            var mdsalLoad = MdsalRealm.prepareForLoad(passwordHashService, dataBroker);
            var moonLoad = MoonRealm.prepareForLoad(servletSupport);
            var tokenAuthLoad = TokenAuthRealm.prepareForLoad(authenticationService, tokenAuthenticators, tokenStore)) {
            // Initialize the Shiro environment from clustered-app-config
            final Ini ini = createIniFromClusteredAppConfig(shiroConfiguration);
            setIni(ini);
            ClassLoaderUtils.runWithClassLoader(AAAIniWebEnvironment.class.getClassLoader(), super::init);
        }
    }
}
