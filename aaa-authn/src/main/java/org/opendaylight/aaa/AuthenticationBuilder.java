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
public class AuthenticationBuilder extends ClaimBuilder implements
        Authentication {

    private long expiration;

    public AuthenticationBuilder() {
    }

    public AuthenticationBuilder(Claim claim) {
        userId = claim.userId();
        user = claim.user();
        domain = claim.domain();
        roles.addAll(claim.roles());
    }

    @Override
    public long expiration() {
        return expiration;
    }

    public AuthenticationBuilder setExpiration(long expiration) {
        this.expiration = expiration;
        return this;
    }

    @Override
    public AuthenticationBuilder setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public AuthenticationBuilder setUser(String userName) {
        this.user = userName;
        return this;
    }

    @Override
    public AuthenticationBuilder setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    @Override
    public AuthenticationBuilder addRoles(Set<String> roles) {
        this.roles.addAll(roles);
        return this;
    }

    @Override
    public AuthenticationBuilder addRole(String role) {
        this.roles.add(role);
        return this;
    }

    @Override
    public Authentication build() {
        return this;
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
