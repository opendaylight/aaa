/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.keystone;

import org.apache.felix.dm.Component;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;

/**
 * An activator for {@link KeystoneTokenAuthFilter}.
 *
 * @author liemmn
 *
 */
public class Activator extends ComponentActivatorAbstractBase {

    @Override
    public Object[] getImplementations() {
        Object[] res = { ServiceLocator.INSTANCE };
        return res;
    }

    @Override
    public void configureInstance(Component c, Object imp, String containerName) {
        if (imp.equals(ServiceLocator.INSTANCE)) {
            c.add(createServiceDependency().setService(
                    AuthenticationService.class).setRequired(true));
        }
    }

}
