/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.ClientService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;

import java.util.LinkedList;
import java.util.List;

/**
 * A service locator to bridge between the web world and OSGi world.
 *
 * @author liemmn
 *
 */
public class ServiceLocator {

    private static final ServiceLocator instance = new ServiceLocator();

    protected volatile List<TokenAuth> tokenAuthCollection = new LinkedList<>();

    protected volatile CredentialAuth<PasswordCredentials> credentialAuth;

    protected volatile TokenStore tokenStore;

    protected volatile AuthenticationService authenticationService;

    protected volatile IdMService idmService;

    protected volatile ClientService clientService;

    private ServiceLocator() {
    }

    public static ServiceLocator getInstance() {
        return instance;
    }

    protected void tokenAuthAdded(TokenAuth ta) {
        this.tokenAuthCollection.add(ta);
    }

    protected void tokenAuthRemoved(TokenAuth ta) {
        this.tokenAuthCollection.remove(ta);
    }

    protected void tokenStoreAdded(TokenStore ts) {
        this.tokenStore = ts;
    }

    protected void tokenStoreRemoved(TokenStore ts) {
        this.tokenStore = null;
    }

    protected void authenticationServiceAdded(AuthenticationService as) {
      this.authenticationService = as;
    }

    protected void authenticationServiceRemoved(AuthenticationService as) {
      this.authenticationService = null;
    }

    protected void credentialAuthAdded(CredentialAuth<PasswordCredentials> da) {
      this.credentialAuth = da;
    }

    protected void credentialAuthAddedRemoved(CredentialAuth<PasswordCredentials> da) {
      this.credentialAuth = null;
    }

    public synchronized List<TokenAuth> getTokenAuthCollection() {
        return tokenAuthCollection;
    }

    public synchronized void setTokenAuthCollection(
            List<TokenAuth> tokenAuthCollection) {
        this.tokenAuthCollection = tokenAuthCollection;
    }

    public synchronized CredentialAuth<PasswordCredentials> getCredentialAuth() {
        return credentialAuth;
    }

    public synchronized void setCredentialAuth(
            CredentialAuth<PasswordCredentials> credentialAuth) {
        this.credentialAuth = credentialAuth;
    }

    public synchronized TokenStore getTokenStore() {
        return tokenStore;
    }

    public synchronized void setTokenStore(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    public synchronized AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public synchronized void setAuthenticationService(
            AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public synchronized IdMService getIdmService() {
        return idmService;
    }

    public synchronized void setIdmService(IdMService idmService) {
        this.idmService = idmService;
    }

    public synchronized ClientService getClientService() {
        return clientService;
    }

    public synchronized void setClientService(ClientService clientService) {
        this.clientService = clientService;
    }
}
