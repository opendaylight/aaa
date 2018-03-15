/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
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
import org.opendaylight.aaa.api.shiro.web.env.ShiroWebEnvironmentLoaderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of ShiroWebEnvironmentLoaderListener.
 *
 * @author Thomas Pantelis
 */
public class ShiroWebEnvironmentLoaderListenerImpl extends EnvironmentLoaderListener
        implements ShiroWebEnvironmentLoaderListener {
    private static final Logger LOG = LoggerFactory.getLogger(ShiroWebEnvironmentLoaderListenerImpl.class);

    private final AAAShiroProvider provider;

    public ShiroWebEnvironmentLoaderListenerImpl(AAAShiroProvider provider) {
        this.provider = provider;
        LOG.debug("ShiroWebEnvironmentLoaderListenerImpl created");
    }

    @Override
    protected WebEnvironment createEnvironment(ServletContext sc) {
        MutableWebEnvironment environment = new AAAIniWebEnvironment(provider);

        // in newer Shiro version, there is a determineWebEnvironment() which should be
        // used instead of createEnvironment() but for 1.3.x we just copy/paste from parent and do:
        environment.setServletContext(sc);
        customizeEnvironment(environment);
        LifecycleUtils.init(environment);
        return environment;
    }
}
