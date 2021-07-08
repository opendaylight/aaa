/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.tokenauthrealm.auth;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.aaa.api.Claim;

/**
 * Builder for a {@link Claim}. The userId, user, and roles information is
 * mandatory.
 *
 * @author liemmn
 */
public class ClaimBuilder {
    private String userId = "";
    private String user = "";
    private final Set<String> roles = new LinkedHashSet<>();
    private String clientId = "";
    private String domain = "";

    public ClaimBuilder() {
    }

    public ClaimBuilder(Claim claim) {
        clientId = claim.clientId();
        userId = claim.userId();
        user = claim.user();
        domain = claim.domain();
        roles.addAll(claim.roles());
    }

    public ClaimBuilder setClientId(String clientId) {
        this.clientId = Strings.nullToEmpty(clientId).trim();
        return this;
    }

    public ClaimBuilder setUserId(String userId) {
        this.userId = Strings.nullToEmpty(userId).trim();
        return this;
    }

    public ClaimBuilder setUser(String userName) {
        user = Strings.nullToEmpty(userName).trim();
        return this;
    }

    public ClaimBuilder setDomain(String domain) {
        this.domain = Strings.nullToEmpty(domain).trim();
        return this;
    }

    public ClaimBuilder addRoles(Set<String> theRoles) {
        for (String role : theRoles) {
            addRole(role);
        }
        return this;
    }

    public ClaimBuilder addRole(String role) {
        roles.add(Strings.nullToEmpty(role).trim());
        return this;
    }

    public Claim build() {
        return new ImmutableClaim(this);
    }

    protected static class ImmutableClaim implements Claim, Serializable {
        private static final long serialVersionUID = -8115027645190209129L;
        private int hashCode = 0;
        protected String clientId;
        protected String userId;
        protected String user;
        protected String domain;
        protected ImmutableSet<String> roles;

        protected ImmutableClaim(ClaimBuilder base) {
            clientId = base.clientId;
            userId = base.userId;
            user = base.user;
            domain = base.domain;
            roles = ImmutableSet.<String>builder().addAll(base.roles).build();

            if (userId.isEmpty() || user.isEmpty() || roles.isEmpty() || roles.contains("")) {
                throw new IllegalStateException(
                        "The Claim is missing one or more of the required fields.");
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

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Claim)) {
                return false;
            }
            Claim claim = (Claim) object;
            return Objects.equals(roles, claim.roles())
                    && Objects.equals(domain, claim.domain())
                    && Objects.equals(userId, claim.userId())
                    && Objects.equals(user, claim.user())
                    && Objects.equals(clientId, claim.clientId());
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                hashCode = Objects.hash(clientId, userId, user, domain, roles);
            }
            return hashCode;
        }

        @Override
        public String toString() {
            return "clientId:" + clientId + "," + "userId:" + userId + "," + "userName:" + user
                    + "," + "domain:" + domain + "," + "roles:" + roles;
        }
    }
}
