/*
 * Copyright (c) 2015 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.opendaylight.aaa.AAAShiroProvider;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Identical to <code>IniWebEnvironment</code> except the Ini is loaded from
 * <code>${KARAF_HOME}/etc/shiro.ini</code>, and setting the TCCL (x2) so that
 * loading of classes by name (from aaa-app-config.xml) works even with
 * ShiroWebContextSecurer.
 *
 * @deprecated in favor of {@link AAAIniWebEnvironment}. This class is kept for compatibility for other projects that
 *             reference it in a web.xml file where it's instantiated via reflection and the dependencies must be
 *             accessed statically.
 */
@Deprecated
public class KarafIniWebEnvironment extends IniWebEnvironment {

    private static final Logger LOG = LoggerFactory.getLogger(KarafIniWebEnvironment.class);

    public KarafIniWebEnvironment() {
        LOG.info("Initializing the Web Environment using {}", KarafIniWebEnvironment.class.getName());
    }

    @Override
    public void init() {
        try {
            AAAShiroProvider provider = AAAShiroProvider.INSTANCE_FUTURE.get();

            ThreadLocals.DATABROKER_TL.set(provider.getDataBroker());
            ThreadLocals.CERT_MANAGER_TL.set(provider.getCertificateManager());
            ThreadLocals.AUTH_SETVICE_TL.set(provider.getAuthenticationService());
            ThreadLocals.TOKEN_AUTHENICATORS_TL.set(provider.getTokenAuthenticators());
            ThreadLocals.TOKEN_STORE_TL.set(provider.getTokenStore());
            ThreadLocals.PASSWORD_HASH_SERVICE_TL.set(provider.getPasswordHashService());

            // Initialize the Shiro environment from clustered-app-config
            final Ini ini = AAAIniWebEnvironment.createIniFromClusteredAppConfig(provider.getShiroConfiguration());
            setIni(ini);
            ClassLoaderUtils.withClassLoader(KarafIniWebEnvironment.class.getClassLoader(), (Supplier<Void>) () -> {
                super.init();
                return null;
            });
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error obtaining AAAShiroProvider", e);
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
