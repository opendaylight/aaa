/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cert.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mserngawy
 *
 */
public class AaaCertMdsalProvider implements AutoCloseable, BindingAwareProvider {

    private final static Logger LOG = LoggerFactory.getLogger(AaaCertMdsalProvider.class);

    public AaaCertMdsalProvider() {
        LOG.info("Initialized AaaCertMdsalProvider");
    }

    /* (non-Javadoc)
     * @see org.opendaylight.controller.sal.binding.api.BindingAwareProvider#onSessionInitiated(org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext)
     */
    @Override
    public void onSessionInitiated(ProviderContext arg0) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
    }

}
