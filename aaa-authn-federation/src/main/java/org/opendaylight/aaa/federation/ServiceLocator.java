/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.federation;

import java.util.LinkedList;
import java.util.List;

import org.opendaylight.aaa.api.ClaimAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.TokenStore;

/**
 * A service locator to bridge between the web world and OSGi world.
 *
 * @author liemmn
 *
 */
public enum ServiceLocator {
    INSTANCE;

    volatile List<ClaimAuth> ca = new LinkedList<>();

    volatile TokenStore ts;

    volatile IdMService is;

    protected void claimAuthAdded(ClaimAuth ca) {
        this.ca.add(ca);
    }

    protected void claimAuthRemoved(ClaimAuth ca) {
        this.ca.remove(ca);
    }
}
