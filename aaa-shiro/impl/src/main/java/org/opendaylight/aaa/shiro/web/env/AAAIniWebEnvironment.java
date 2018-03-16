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
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.shiro.configuration.Main;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.shiro.configuration.Urls;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends <code>IniWebEnvironment</code> to provide the Ini configuration via a clustered app config,
 * and setts the TCCL (x2) so that loading of classes by name (from aaa-app-config.xml) works even with
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

    AAAIniWebEnvironment(ShiroConfiguration shiroConfiguration, DataBroker dataBroker,
            ICertificateManager certificateManager) {
        this.shiroConfiguration = shiroConfiguration;
        this.dataBroker = dataBroker;
        this.certificateManager = certificateManager;
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
        final SecurityManager securityManager = ClassLoaderUtils.withClassLoader(
                AAAIniWebEnvironment.class.getClassLoader(), (Supplier<SecurityManager>) factory::getInstance);
        SecurityUtils.setSecurityManager(securityManager);

        return ini;
    }

    @Override
    public void init() {
        ThreadLocals.DATABROKER_TL.set(dataBroker);
        ThreadLocals.CERT_MANAGER_TL.set(certificateManager);
        try {
            // Initialize the Shiro environment from clustered-app-config
            final Ini ini = createIniFromClusteredAppConfig(shiroConfiguration);
            setIni(ini);
            ClassLoaderUtils.withClassLoader(AAAIniWebEnvironment.class.getClassLoader(), (Supplier<Void>) () -> {
                super.init();
                return null;
            });
        } finally {
            ThreadLocals.DATABROKER_TL.remove();
            ThreadLocals.CERT_MANAGER_TL.remove();
        }
    }
}
