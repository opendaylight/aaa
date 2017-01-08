/*
 * Copyright © 2017 Brocade Communications Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAAShiroProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroProvider.class);

    private static DataBroker dataBroker;

    private static AAAShiroProvider INSTANCE;

    public AAAShiroProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        INSTANCE = this;
    }

    public static AAAShiroProvider getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AAAShiroProvider(null);
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

    public DataBroker getDataBroker() {
        return this.dataBroker;
    }
}