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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.opendaylight.aaa.api.Claim;

/**
 * Builder for a {@link Claim}
 *
 * @author liemmn
 *
 */
public class ClaimBuilder {
    private final MutableClaim mc = new MutableClaim();

    public ClaimBuilder() {
    }

    public ClaimBuilder(Claim claim) {
        setClaim(claim);
    }

    protected void setClaim(Claim claim) {
        mc.userId = claim.userId();
        mc.user = claim.user();
        mc.domain = claim.domain();
        mc.roles.addAll(claim.roles());
    }

    public ClaimBuilder setUserId(String userId) {
        mc.userId = userId;
        return this;
    }

    public ClaimBuilder setUser(String userName) {
        mc.user = userName;
        return this;
    }

    public ClaimBuilder setDomain(String domain) {
        mc.domain = domain;
        return this;
    }

    public ClaimBuilder addRoles(Set<String> roles) {
        mc.roles.addAll(roles);
        return this;
    }

    public ClaimBuilder addRole(String role) {
        mc.roles.add(role);
        return this;
    }

    public Claim build() {
        return mc;
    }

    // Mutable claim
    protected static class MutableClaim implements Claim {
        int hashCode = 0;
        String userId;
        String user;
        String domain;
        final Set<String> roles = new HashSet<String>();

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
            return Collections.unmodifiableSet(roles);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Claim))
                return false;
            Claim a = (Claim) o;
            return areEqual(roles, a.roles()) && areEqual(domain, a.domain())
                    && areEqual(userId, a.userId()) && areEqual(user, a.user());
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                int result = HashCodeUtil.SEED;
                result = hash(result, userId);
                result = hash(result, user);
                result = hash(result, domain);
                result = hash(result, roles);
                hashCode = result;
            }
            return hashCode;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            if (userId != null)
                sb.append("userId:").append(userId).append(",");
            if (user != null)
                sb.append("userName:").append(user).append(",");
            if (domain != null)
                sb.append("domain:").append(domain).append(",");
            sb.append("roles:").append(roles);
            return sb.toString();
        }
    }

}
