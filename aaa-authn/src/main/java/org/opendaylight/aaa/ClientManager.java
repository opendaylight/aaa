/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.ClientService;

/**
 * A configuration-based client manager.
 *
 * @author liemmn
 *
 */
public class ClientManager implements ClientService {

    @Override
    public void validate(String clientId, String clientSecret)
            throws AuthenticationException {
        // TODO Use configuration to validate valid clients for Helium
        // Post-Helium, we will support a CRUD API
    }

}
