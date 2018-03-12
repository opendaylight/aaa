/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import javax.servlet.ServletContext;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.MutableWebEnvironment;
import org.apache.shiro.web.env.WebEnvironment;
import org.opendaylight.aaa.AAAShiroProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shiro EnvironmentLoaderListener providing the WebEnvironmentClass,
 * instead of specifying the {@link KarafIniWebEnvironment} via
 * "shiroEnvironmentClass" context parameter as String.
 *
 * @author Michael Vorburger.ch
 */
public class KarafIniWebEnvironmentLoaderListener extends EnvironmentLoaderListener {

    private static final Logger LOG = LoggerFactory.getLogger(KarafIniWebEnvironmentLoaderListener.class);

    private final AAAShiroProvider provider;

    public KarafIniWebEnvironmentLoaderListener(AAAShiroProvider provider) {
        this.provider = provider;
        LOG.info("new KarafIniWebEnvironmentLoaderListener() : {}", this);
    }

    @Override
    protected WebEnvironment createEnvironment(ServletContext sc) {
        MutableWebEnvironment environment = new KarafIniWebEnvironment(provider);

        // in newer Shiro version, there is a determineWebEnvironment() which should be
        // used instead of createEnvironment() but for 1.3.x we just copy/paste from parent and do:
        environment.setServletContext(sc);
        customizeEnvironment(environment);
        LifecycleUtils.init(environment);
        return environment;
    }
}
