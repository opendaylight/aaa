/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import static org.opendaylight.aaa.EqualUtil.areEqual;
import static org.opendaylight.aaa.HashCodeUtil.hash;

import java.util.Set;

import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.Claim;

/**
 * A builder for the authentication context.
 *
 * @author liemmn
 *
 */
public class AuthenticationBuilder extends ClaimBuilder {

    private final MutableAuthentication ma = new MutableAuthentication();

    public AuthenticationBuilder() {
    }

    public AuthenticationBuilder(Claim claim) {
        setClaim(claim);
    }

    public AuthenticationBuilder setExpiration(long expiration) {
        ma.expiration = expiration;
        return this;
    }

    @Override
    protected void setClaim(Claim claim) {
        ma.clientId = claim.clientId();
        ma.userId = claim.userId();
        ma.user = claim.user();
        ma.domain = claim.domain();
        ma.roles.addAll(claim.roles());
    }

    @Override
    public AuthenticationBuilder setClientId(String clientId) {
        ma.clientId = clientId;
        return this;
    }

    @Override
    public AuthenticationBuilder setUserId(String userId) {
        ma.userId = userId;
        return this;
    }

    @Override
    public AuthenticationBuilder setUser(String userName) {
        ma.user = userName;
        return this;
    }

    @Override
    public AuthenticationBuilder setDomain(String domain) {
        ma.domain = domain;
        return this;
    }

    @Override
    public AuthenticationBuilder addRoles(Set<String> roles) {
        ma.roles.addAll(roles);
        return this;
    }

    @Override
    public AuthenticationBuilder addRole(String role) {
        ma.roles.add(role);
        return this;
    }

    @Override
    public Authentication build() {
        return ma;
    }

    // Mutable Authentication
    protected static class MutableAuthentication extends
            ClaimBuilder.MutableClaim implements Authentication {
        private static final long serialVersionUID = 4919078164955609987L;
        long expiration = 0L;

        @Override
        public long expiration() {
            return expiration;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Authentication))
                return false;
            Authentication a = (Authentication) o;
            return areEqual(expiration, a.expiration()) && super.equals(o);
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int result = HashCodeUtil.SEED;
                result = hash(result, expiration);
                result = hash(result, super.hashCode());
                hashCode = result;
            }
            return hashCode;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("expiration:").append(expiration).append(",");
            sb.append(super.toString());
            return sb.toString();
        }
    }
}
