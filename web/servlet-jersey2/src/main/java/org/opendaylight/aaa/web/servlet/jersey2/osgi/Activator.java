/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.servlet.jersey2.osgi;

import org.opendaylight.aaa.web.servlet.ServletSupport;
import org.opendaylight.aaa.web.servlet.jersey2.JerseyServletSupport;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
    private ServiceRegistration<ServletSupport> reg;

    @Override
    public void start(final BundleContext context) {
        reg = context.registerService(ServletSupport.class, new JerseyServletSupport(), null);
    }

    @Override
    public void stop(final BundleContext context) {
        if (reg != null) {
            reg.unregister();
            reg = null;
        }
    }
}
