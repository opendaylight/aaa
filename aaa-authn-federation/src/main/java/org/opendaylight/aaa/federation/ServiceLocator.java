/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.federation;

import java.util.List;
import java.util.Vector;
import org.opendaylight.aaa.api.ClaimAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.TokenStore;

/**
 * A service locator to bridge between the web world and OSGi world.
 *
 * @author liemmn
 *
 */
@Deprecated
public class ServiceLocator {

    private static final ServiceLocator instance = new ServiceLocator();

    protected volatile List<ClaimAuth> claimAuthCollection = new Vector<>();

    protected volatile TokenStore tokenStore;

    protected volatile IdMService idmService;

    private ServiceLocator() {
    }

    public static ServiceLocator getInstance() {
        return instance;
    }

    /**
     * Called through reflection from the federation Activator
     *
     * @see org.opendaylight.aaa.federation.ServiceLocator
     * @param ca the injected claims implementation
     */
    protected void claimAuthAdded(ClaimAuth ca) {
        this.claimAuthCollection.add(ca);
    }

    /**
     * Called through reflection from the federation Activator
     *
     * @see org.opendaylight.aaa.federation.Activator
     * @param ca the claims implementation to remove
     */
    protected void claimAuthRemoved(ClaimAuth ca) {
        this.claimAuthCollection.remove(ca);
    }

    public List<ClaimAuth> getClaimAuthCollection() {
        return claimAuthCollection;
    }

    public void setClaimAuthCollection(List<ClaimAuth> claimAuthCollection) {
        this.claimAuthCollection = claimAuthCollection;
    }

    public TokenStore getTokenStore() {
        return tokenStore;
    }

    public void setTokenStore(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    public IdMService getIdmService() {
        return idmService;
    }

    public void setIdmService(IdMService idmService) {
        this.idmService = idmService;
    }
}
