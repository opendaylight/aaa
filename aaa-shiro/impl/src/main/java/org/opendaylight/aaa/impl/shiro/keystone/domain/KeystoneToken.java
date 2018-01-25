/*
 * Copyright (c) 2017 Ericsson Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.impl.shiro.keystone.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Keystone API v3 Token object.
 */
public class KeystoneToken {
    private Token token;

    public KeystoneToken() {}

    public KeystoneToken(Token theToken) {
        token = theToken;
    }

    public void setToken(Token theToken) {
        token = theToken;
    }

    public Token getToken() {
        return token;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Token {

        private List<Role> roles = new ArrayList<>();

        public Token() {}

        public Token(List<Role> theRoles) {
            roles.addAll(theRoles);
        }

        public void addRoles(List<Role> theRoles) {
            roles.addAll(theRoles);
        }

        public List<Role> getRoles() {
            return roles;
        }

        public static final class Role {
            private String name;

            private String id;

            public Role() {}

            public Role(String theRoleName, String theId) {
                name = theRoleName;
                id = theId;
            }

            public void setName(String theRoleName) {
                name = theRoleName;
            }

            public String getName() {
                return name;
            }

            public void setId(String theId) {
                id = theId;
            }

            public String getId() {
                return id;
            }
        }
    }
}
