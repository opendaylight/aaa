/*
 * Copyright (c) 2015 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import java.util.concurrent.ExecutionException;
import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.opendaylight.aaa.AAAShiroProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Identical to <code>IniWebEnvironment</code> except the Ini is loaded from
 * <code>${KARAF_HOME}/etc/shiro.ini</code>.
 *
 * @deprecated in favor of {@link AAAIniWebEnvironment}. This class is kept for compatibility for other projects that
 *             reference it in a web.xml file where it's instantiated via reflection and the dependencies must be
 *             accessed statically.
 */
@Deprecated
public class KarafIniWebEnvironment extends IniWebEnvironment {

    private static final Logger LOG = LoggerFactory.getLogger(KarafIniWebEnvironment.class);

    public KarafIniWebEnvironment() {
        LOG.info("Initializing the Web Environment using {}",
                KarafIniWebEnvironment.class.getName());
    }

    @Override
    public void init() {
        try {
            AAAShiroProvider provider = AAAShiroProvider.INSTANCE_FUTURE.get();

            ThreadLocals.DATABROKER_TL.set(provider.getDataBroker());
            ThreadLocals.CERT_MANAGER_TL.set(provider.getCertificateManager());

            // Initialize the Shiro environment from clustered-app-config
            final Ini ini = AAAIniWebEnvironment.createIniFromClusteredAppConfig(provider.getShiroConfiguration());
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
