/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.tokenauthrealm.auth;

import java.util.Objects;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.shiro.tokenauthrealm.util.EqualUtil;

/**
 * {@link PasswordCredentials} builder.
 *
 * @author liemmn
 */
public class PasswordCredentialBuilder {
    private final MutablePasswordCredentials pc = new MutablePasswordCredentials();

    public PasswordCredentialBuilder setUserName(String username) {
        pc.username = username;
        return this;
    }

    public PasswordCredentialBuilder setPassword(String password) {
        pc.password = password;
        return this;
    }

    public PasswordCredentialBuilder setDomain(String domain) {
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
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof PasswordCredentials)) {
                return false;
            }
            PasswordCredentials passwordCredentials = (PasswordCredentials) object;
            return EqualUtil.areEqual(username, passwordCredentials.username())
                    && EqualUtil.areEqual(password, passwordCredentials.password());
        }

        @Override
        public int hashCode() {
            if (hashCode == 0) {
                hashCode = Objects.hash(username, password);
            }
            return hashCode;
        }
    }
}
