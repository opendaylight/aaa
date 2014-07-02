/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm;

import org.apache.felix.dm.Component;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;

/**
 * An activator to publish the {@link CredentialAuth} implementation provided by
 * this bundle into OSGi.
 *
 * @author liemmn
 *
 */
public class Activator extends ComponentActivatorAbstractBase {
    @Override
    public Object[] getImplementations() {
        Object[] res = { IdmLightProxy.class };
        return res;
    }

    @Override
    public void configureInstance(Component c, Object imp, String containerName) {
        if (imp.equals(IdmLightProxy.class)) {
            c.setInterface(new String[] { CredentialAuth.class.getName(),
                    IdMService.class.getName() }, null);
        }
    }
}
