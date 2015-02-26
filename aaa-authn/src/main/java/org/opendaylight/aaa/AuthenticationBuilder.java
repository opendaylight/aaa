/*
 * Copyright (c) 2014-2015 Hewlett-Packard Development Company, L.P. and others.
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
 * A builder for the authentication context. The expiration, userId, user, and roles information is mandatory.
 *
 * @author liemmn
 *
 */
public class AuthenticationBuilder extends ClaimBuilder {

    private long expiration = 0L;

    public AuthenticationBuilder() {
    }

    public AuthenticationBuilder(Claim claim) {
        setClaim(claim);
    }

    public AuthenticationBuilder setExpiration(long expiration) {
        this.expiration = expiration;
        return this;
    }

    @Override
    protected void setClaim(Claim claim) {
        super.setClientId(claim.clientId());
        super.setUserId(claim.userId());
        super.setUser(claim.user());
        super.setDomain(claim.domain());
        super.addRoles(claim.roles());
    }

    @Override
    public AuthenticationBuilder setClientId(String clientId) {
        super.setClientId(clientId);
        return this;
    }

    @Override
    public AuthenticationBuilder setUserId(String userId) {
        super.setUserId(userId);
        return this;
    }

    @Override
    public AuthenticationBuilder setUser(String userName) {
        super.setUser(userName);
        return this;
    }

    @Override
    public AuthenticationBuilder setDomain(String domain) {
        super.setDomain(domain);
        return this;
    }

    @Override
    public AuthenticationBuilder addRoles(Set<String> roles) {
        super.addRoles(roles);
        return this;
    }

    @Override
    public AuthenticationBuilder addRole(String role) {
        super.addRole(role);
        return this;
    }

    @Override
    public Authentication build() {
        return new ImmutableAuthentication(this);
    }

    protected static class ImmutableAuthentication extends
        ImmutableClaim implements Authentication {
        private static final long serialVersionUID = 4919078164955609987L;
        private int hashCode = 0;
        long expiration = 0L;

        protected ImmutableAuthentication(AuthenticationBuilder base) {
            super(base);
            expiration = base.expiration;

            if (base.expiration < 0) {
                throw new IllegalStateException("The expiration is less than 0.");
            }
        }

        @Override
        public long expiration() {
            return expiration;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Authentication)) {
                return false;
            }
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
            StringBuilder sb = new StringBuilder();
            sb.append("expiration:").append(expiration).append(",");
            sb.append(super.toString());
            return sb.toString();
        }
    }
}
