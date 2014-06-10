/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import org.apache.felix.dm.Component;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;

/**
 * Activator to register {@link AuthenticationService} with OSGi.
 *
 * @author liemmn
 *
 */
public class Activator extends ComponentActivatorAbstractBase {

    @Override
    public Object[] getImplementations() {
        return new Object[] { AuthenticationManager.instance() };
    }

    @Override
    public void configureInstance(Component c, Object impl, String containerName) {
        if (impl.equals(AuthenticationManager.instance())) {
            // export service
            c.setInterface(
                    new String[] { AuthenticationService.class.getName() },
                    null);
        }
    }

}
