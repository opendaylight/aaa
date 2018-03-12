/*
 * Copyright (c) 2015 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.opendaylight.aaa.AAAShiroProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.shiro.configuration.Main;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.shiro.configuration.Urls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Identical to <code>IniWebEnvironment</code> except the Ini is loaded from
 * <code>${KARAF_HOME}/etc/shiro.ini</code>.
 */
public class KarafIniWebEnvironment extends IniWebEnvironment {

    private static final Logger LOG = LoggerFactory.getLogger(KarafIniWebEnvironment.class);

    private static final String MAIN_SECTION_HEADER = "main";
    private static final String URLS_SECTION_HEADER = "urls";

    public KarafIniWebEnvironment() {
        LOG.info("Initializing the Web Environment using {}",
                KarafIniWebEnvironment.class.getName());
    }

    private static Ini createIniFromClusteredAppConfig(final ShiroConfiguration shiroConfiguration) {
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
        final SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);

        return ini;
    }

    @Override
    public void init() {
        try {
            AAAShiroProvider provider = AAAShiroProvider.INSTANCE_FUTURE.get();

            ThreadLocals.DATABROKER_TL.set(provider.getDataBroker());
            ThreadLocals.CERT_MANAGER_TL.set(provider.getCertificateManager());

            // Initialize the Shiro environment from clustered-app-config
            final Ini ini = createIniFromClusteredAppConfig(provider.getShiroConfiguration());
            setIni(ini);
            super.init();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error obtaining AAAShiroProvider", e);
        } finally {
            ThreadLocals.DATABROKER_TL.remove();
            ThreadLocals.CERT_MANAGER_TL.remove();
        }
    }
}
