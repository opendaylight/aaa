/*
 * Copyright (c) 2016 Orange and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.moon;

import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.util.Set;

import org.opendaylight.aaa.api.Claim;

/**
 * MoonPrincipal contains all user's information returned by moon on successful authentication
 *
 * @author Alioune BA alioune.ba@orange.com
 */
public class MoonPrincipal {

    private final String username;
    private final String domain;
    private final String userId;
    private final ImmutableSet<String> roles;
    private final String token;


    public MoonPrincipal(final String username, final String domain, final String userId,
            final Set<String> roles, final String token) {

        this.username = username;
        this.domain = domain;
        this.userId = userId;
        this.roles = ImmutableSet.copyOf(roles);
        this.token = token;
    }

    public MoonPrincipal createODLPrincipal(final String username, final String domain,
            final String userId, final Set<String> roles, final String token) {

        return new MoonPrincipal(username, domain, userId, roles,token);
    }

    public Claim principalToClaim () {
        return new MoonClaim("", this.getUserId(), this.getUsername(), this.getDomain(), this.getRoles());
    }

    public String getUsername() {
        return this.username;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getUserId() {
        return this.userId;
    }

    public Set<String> getRoles() {
        return this.roles;
    }

    public String getToken(){
        return this.token;
    }

    public class MoonClaim implements Claim, Serializable {

        private String clientId;
        private String userId;
        private String user;
        private String domain;
        private ImmutableSet<String> roles;

        public MoonClaim(final String clientId, final String userId, final String user,
                final String domain, final Set<String> roles) {

            this.clientId = clientId;
            this.userId = userId;
            this.user = user;
            this.domain = domain;
            this.roles = ImmutableSet.copyOf(roles);

            if (userId.isEmpty() || user.isEmpty() || roles.isEmpty() || roles.contains("")) {
                throw new IllegalStateException("The Claim is missing one or more of the required fields.");
            }
        }

        @Override
        public String clientId() {
            return clientId;
        }

        @Override
        public String userId() {
            return userId;
        }

        @Override
        public String user() {
            return user;
        }

        @Override
        public String domain() {
            return domain;
        }

        @Override
        public Set<String> roles() {
            return roles;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public ImmutableSet<String> getRoles() {
            return roles;
        }

        public void setRoles(ImmutableSet<String> roles) {
            this.roles = ImmutableSet.copyOf(roles);
        }

        @Override
        public String toString() {
            return "clientId:" + clientId + "," + "userId:" + userId + "," + "userName:" + user
                    + "," + "domain:" + domain + "," + "roles:" + roles ;
        }
    }
}
