/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import java.util.Enumeration;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * AuthenticationToken used to pass information forwarded by oauth2-proxy. Contains no credential as we rely on user
 * being successfully authorized by identity provider server.
 *
 * @param groups groups or roles of the user
 * @param user user identifier
 */
public record Oauth2ProxyToken(Enumeration<String> groups, String user) implements AuthenticationToken {
    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
