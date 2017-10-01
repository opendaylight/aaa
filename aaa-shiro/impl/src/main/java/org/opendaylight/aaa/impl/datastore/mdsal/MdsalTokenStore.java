/*
 * Copyright (c) 2017 Kontron company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.impl.datastore.mdsal;

import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenStore;

public class MdsalTokenStore implements AutoCloseable, TokenStore {

    private final long timeToLive;

    public MdsalTokenStore(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override
    public void put(String token, Authentication auth) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Authentication get(String token) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean delete(String token) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long tokenExpiration() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
    }

}
