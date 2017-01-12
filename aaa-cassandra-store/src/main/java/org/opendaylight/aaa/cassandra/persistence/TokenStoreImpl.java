/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cassandra.persistence;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author saichler@gmail.com
 */
@Deprecated
public class TokenStoreImpl extends AbstractStore<AAAToken,AAATokens> implements TokenStore{
    private static final Logger LOG = LoggerFactory.getLogger(TokenStoreImpl.class);
    private static final long TOKEN_EXPERATION = 86400000;
    public TokenStoreImpl(CassandraStore store) throws NoSuchMethodException {
        super(store, AAAToken.class, AAATokens.class, "setTokens", "setAaaToken");
    }

    @Override
    public void put(String token, Authentication auth) {
        AAAToken t = new AAAToken();
        t.setAaaToken(token);
        t.setClientId(auth.clientId());
        t.setDomain(auth.domain());
        t.setExperation(auth.expiration());
        t.setUserId(auth.userId());
        t.setUsername(auth.user());
        String roles = "";
        boolean first = true;
        for(String role:auth.roles()){
            if(!first){
                roles+=",";
            }
            roles+=role;
            first = false;
        }
        t.setRoles(roles);
        try {
            this.createElement(t);
        } catch (IDMStoreException e) {
            LOG.error("Failed to save token to store",e);
        }
    }

    @Override
    public synchronized Authentication get(String token) {
        try {
            return new CassandraAuthentication(getElement(token));
        } catch (IDMStoreException e) {
            LOG.error("Failed to get token from store", e);
        }
        return null;
    }

    @Override
    public boolean delete(String token) {
        try {
            return deleteElement(token)!=null;
        } catch (IDMStoreException e) {
            LOG.error("Failed to delete token from store",e);
        }
        return false;
    }

    @Override
    public synchronized long tokenExpiration() {
        return TOKEN_EXPERATION;
    }

    public static class CassandraAuthentication implements Authentication{

        private final AAAToken token;

        public CassandraAuthentication(AAAToken token){
            this.token = token;
        }

        @Override
        public long expiration() {
            return token.getExperation();
        }

        @Override
        public String clientId() {
            return token.getClientId();
        }

        @Override
        public String userId() {
            return token.getUserId();
        }

        @Override
        public String user() {
            return token.getUsername();
        }

        @Override
        public String domain() {
            return token.getDomain();
        }

        @Override
        public Set<String> roles() {
            Set<String> result = new HashSet<>();
            StringTokenizer st = new StringTokenizer(token.getRoles(),",");
            while(st.hasMoreTokens()){
                result.add(st.nextToken());
            }
            return result;
        }
    }
}