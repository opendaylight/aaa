/*
 * Copyright © 2017 Brocade Communications Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl;

import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.controller.config.api.osgi.WaitingServiceTracker;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider for AAA shiro implementation.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class AAAShiroProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroProvider.class);

    private static AAAShiroProvider INSTANCE;
    private DataBroker dataBroker;
    private final ICertificateManager certificateManager;

    /**
     * Provider for this bundle.
     *
     * @param dataBroker injected from blueprint
     */
    private AAAShiroProvider(final DataBroker dataBroker, final ICertificateManager certificateManager) {
        this.dataBroker = dataBroker;
        this.certificateManager = certificateManager;
    }

    /**
     * Singleton creation
     *
     * @return the Provider
     */
    public static AAAShiroProvider newInstance(final DataBroker dataBroker,
                                               final ICertificateManager certificateManager) {

        // because the ClassLoadingStrategy Service is advertised by the config manager,
        // we need to use a WaitingServiceTracker to make sure it is good to go.
        final BundleContext bundleContext =
                FrameworkUtil.getBundle(AAAShiroProvider.class).getBundleContext();
        final WaitingServiceTracker<ClassLoadingStrategy> clsWaitingServiceTracker =
                WaitingServiceTracker.create(ClassLoadingStrategy.class, bundleContext);
        clsWaitingServiceTracker.waitForService(WaitingServiceTracker.FIVE_MINUTES);

        INSTANCE = new AAAShiroProvider(dataBroker, certificateManager);
        return INSTANCE;
    }

    /**
     * Singleton extraction
     *
     * @return the Provider
     */
    public static AAAShiroProvider getInstance() {
        if (INSTANCE == null) {
            newInstance(null, null);
        }
        return INSTANCE;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("AAAShiroProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("AAAShiroProvider Closed");
    }

    /**
     * Extract the data broker.
     *
     * @return the data broker
     */
    public DataBroker getDataBroker() {
        return this.dataBroker;
    }

    /**
     * Extract the certificate manager.
     *
     * @return the certificate manager.
     */
    public ICertificateManager getCertificateManager() {
        return certificateManager;
    }
}
