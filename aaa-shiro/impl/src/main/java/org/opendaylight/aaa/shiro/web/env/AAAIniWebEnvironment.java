/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import java.util.List;
import java.util.function.Supplier;
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
import org.opendaylight.aaa.shiro.tokenauthrealm.auth.TokenAuthenticators;
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
class AAAIniWebEnvironment extends IniWebEnvironment {
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

    AAAIniWebEnvironment(final ShiroConfiguration shiroConfiguration, final DataBroker dataBroker,
                         final ICertificateManager certificateManager,
                         final AuthenticationService authenticationService,
                         final TokenAuthenticators tokenAuthenticators, final TokenStore tokenStore,
                         final PasswordHashService passwordHashService) {
        this.shiroConfiguration = shiroConfiguration;
        this.dataBroker = dataBroker;
        this.certificateManager = certificateManager;
        this.authenticationService = authenticationService;
        this.tokenAuthenticators = tokenAuthenticators;
        this.tokenStore = tokenStore;
        this.passwordHashService = passwordHashService;
        LOG.debug("AAAIniWebEnvironment created");
    }

    static Ini createIniFromClusteredAppConfig(final ShiroConfiguration shiroConfiguration) {
        final Ini ini = new Ini();

        final Ini.Section mainSection = ini.addSection(MAIN_SECTION_HEADER);
        final List<Main> mains = shiroConfiguration.getMain();
        for (final Main main : mains) {
            mainSection.put(main.getPairKey(), main.getPairValue());
        }

        final Ini.Section urlsSection = ini.addSection(URLS_SECTION_HEADER);
        final List<Urls> urls = shiroConfiguration.getUrls();
        for (final Urls url : urls) {
            urlsSection.put(url.getPairKey(), url.getPairValue());
        }

        final Factory<SecurityManager> factory = new IniSecurityManagerFactory(ini);
        final SecurityManager securityManager = ClassLoaderUtils.getWithClassLoader(
                AAAIniWebEnvironment.class.getClassLoader(), (Supplier<SecurityManager>) factory::getInstance);
        SecurityUtils.setSecurityManager(securityManager);

        return ini;
    }

    @Override
    public void init() {
        ThreadLocals.DATABROKER_TL.set(dataBroker);
        ThreadLocals.CERT_MANAGER_TL.set(certificateManager);
        ThreadLocals.AUTH_SETVICE_TL.set(authenticationService);
        ThreadLocals.TOKEN_AUTHENICATORS_TL.set(tokenAuthenticators);
        ThreadLocals.TOKEN_STORE_TL.set(tokenStore);
        ThreadLocals.PASSWORD_HASH_SERVICE_TL.set(passwordHashService);
        try {
            // Initialize the Shiro environment from clustered-app-config
            final Ini ini = createIniFromClusteredAppConfig(shiroConfiguration);
            setIni(ini);
            ClassLoaderUtils.getWithClassLoader(AAAIniWebEnvironment.class.getClassLoader(), (Supplier<Void>) () -> {
                super.init();
                return null;
            });
        } finally {
            ThreadLocals.DATABROKER_TL.remove();
            ThreadLocals.CERT_MANAGER_TL.remove();
            ThreadLocals.AUTH_SETVICE_TL.remove();
            ThreadLocals.TOKEN_AUTHENICATORS_TL.remove();
            ThreadLocals.TOKEN_STORE_TL.remove();
            ThreadLocals.PASSWORD_HASH_SERVICE_TL.remove();
        }
    }
}
