/*
 * Copyright (c) 2017 Ericsson Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.keystone.domain;

/**
 * This class is a representation of a Keystone API v3 Auth object.
 */
public class KeystoneAuth {
    private final Auth auth;

    public KeystoneAuth(String username, String password, String domain) {
        this.auth = new Auth(username, password, domain);
    }

    public Auth getAuth() {
        return auth;
    }

    public static final class Auth {
        private final Identity identity;
        private final Scope scope;

        private Auth(String username, String password, String domain) {
            this.identity = new Identity(username, password, domain);
            this.scope = new Scope(domain);
        }

        public Identity getIdentity() {
            return identity;
        }

        public Scope getScope() {
            return scope;
        }

        public static final class Identity {
            private static final String METHOD_PASSWORD = "password";

            private final String[] methods;
            private final Password password;

            private Identity(String username, String password, String domain) {
                this.password = new Password(username, password, domain);
                this.methods = new String[]{ METHOD_PASSWORD };
            }

            public String[] getMethods() {
                return methods == null ? null : methods.clone();
            }

            public Password getPassword() {
                return password;
            }

            public static final class Password {
                private final User user;

                private Password(String username, String password, String domain) {
                    this.user = new User(username, password, domain);
                }

                public User getUser() {
                    return user;
                }

                public static final class User {
                    private final String name;
                    private final String password;
                    private final Domain domain;

                    private User(String name, String password, String domain) {
                        this.name = name;
                        this.password = password;
                        this.domain = new Domain(domain);
                    }

                    public String getName() {
                        return name;
                    }

                    public String getPassword() {
                        return password;
                    }

                    public Domain getDomain() {
                        return domain;
                    }

                    public static final class Domain {
                        private final String name;

                        private Domain(String name) {
                            this.name = name;
                        }

                        public String getName() {
                            return name;
                        }
                    }
                }

            }
        }

        public static final class Scope {
            private final Domain domain;

            public Scope(String domainId) {
                domain = new Domain(domainId);
            }

            public Domain getDomain() {
                return domain;
            }

            public static final class Domain {
                private final String name;

                public Domain(String theDomain) {
                    name = theDomain;
                }

                public String getName() {
                    return name;
                }
            }
        }
    }
}
