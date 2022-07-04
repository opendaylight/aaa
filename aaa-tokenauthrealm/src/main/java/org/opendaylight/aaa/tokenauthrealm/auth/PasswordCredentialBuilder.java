/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.tokenauthrealm.auth;

import java.util.Objects;
import org.opendaylight.aaa.api.PasswordCredentials;

/**
 * {@link PasswordCredentials} builder.
 *
 * @author liemmn
 */
public class PasswordCredentialBuilder {
    private final MutablePasswordCredentials pc = new MutablePasswordCredentials();

    public PasswordCredentialBuilder setUserName(final String username) {
        pc.username = username;
        return this;
    }

    public PasswordCredentialBuilder setPassword(final String password) {
        pc.password = password;
        return this;
    }

    public PasswordCredentialBuilder setDomain(final String domain) {
        pc.domain = domain;
        return this;
    }

    public PasswordCredentials build() {
        return pc;
    }

    private static class MutablePasswordCredentials implements PasswordCredentials {
        private int hashCode = 0;
        private String username;
        private String password;
        private String domain;

        @Override
        public String username() {
            return username;
        }

        @Override
        public String password() {
            return password;
        }

        @Override
        public String domain() {
            return domain;
        }

        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj instanceof PasswordCredentials other
                && Objects.equals(username, other.username())
                && Objects.equals(password, other.password())
                && Objects.equals(domain, other.domain());
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                hashCode = Objects.hash(username, password, domain);
            }
            return hashCode;
        }
    }
}
