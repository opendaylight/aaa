/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.tokenauthrealm.auth;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.Claim;

/**
 * A builder for the authentication context.
 *
 * <p>
 * The expiration DEFAULTS to 0.
 *
 * @author liemmn
 */
public class AuthenticationBuilder {

    private long expiration = 0L;
    private final Claim claim;

    public AuthenticationBuilder(Claim claim) {
        this.claim = claim;
    }

    public AuthenticationBuilder setExpiration(long expiration) {
        this.expiration = expiration;
        return this;
    }

    public Authentication build() {
        return new ImmutableAuthentication(this);
    }

    private static final class ImmutableAuthentication implements Authentication, Serializable {
        private static final long serialVersionUID = 4919078164955609987L;
        private int hashCode = 0;
        long expiration = 0L;
        Claim claim;

        private ImmutableAuthentication(AuthenticationBuilder base) {
            if (base.claim == null) {
                throw new IllegalStateException("The Claim is null.");
            }
            claim = new ClaimBuilder(base.claim).build();
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
        public String clientId() {
            return claim.clientId();
        }

        @Override
        public String userId() {
            return claim.userId();
        }

        @Override
        public String user() {
            return claim.user();
        }

        @Override
        public String domain() {
            return claim.domain();
        }

        @Override
        public Set<String> roles() {
            return claim.roles();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Authentication)) {
                return false;
            }
            Authentication authentication = (Authentication) object;
            return expiration == authentication.expiration()
                    && Objects.equals(claim.roles(), authentication.roles())
                    && Objects.equals(claim.domain(), authentication.domain())
                    && Objects.equals(claim.userId(), authentication.userId())
                    && Objects.equals(claim.user(), authentication.user())
                    && Objects.equals(claim.clientId(), authentication.clientId());
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                hashCode = Objects.hash(expiration, claim);
            }
            return hashCode;
        }

        @Override
        public String toString() {
            return "expiration:" + expiration + "," + claim.toString();
        }
    }
}
