/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import javax.servlet.ServletContext;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shiro EnvironmentLoaderListener with static WebEnvironmentClass,
 * instead of specifying the {@link KarafIniWebEnvironment} via
 * "shiroEnvironmentClass" context parameter as String.
 *
 * @author Michael Vorburger.ch
 */
public class KarafIniWebEnvironmentLoaderListener extends EnvironmentLoaderListener {

    private static final Logger LOG = LoggerFactory.getLogger(KarafIniWebEnvironmentLoaderListener.class);

    public KarafIniWebEnvironmentLoaderListener() {
        LOG.info("new KarafIniWebEnvironmentLoaderListener() : {}", this);
    }

    @Override
    protected Class<?> determineWebEnvironmentClass(ServletContext servletContext) {
        return KarafIniWebEnvironment.class;
    }

}
