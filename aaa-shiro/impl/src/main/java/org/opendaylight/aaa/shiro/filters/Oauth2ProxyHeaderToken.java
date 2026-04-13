/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import java.util.Set;
import org.apache.shiro.authc.AuthenticationToken;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link AuthenticationToken} used to pass information forwarded by oauth2-proxy.
 *
 * <p>Contains no credential as we rely on user being successfully authenticated by identity provider server.
 *
 * @param groups groups or roles of the user
 * @param user user identifier
 */
public record Oauth2ProxyHeaderToken(Set<String> groups, @Nullable String user) implements AuthenticationToken {
    public Oauth2ProxyHeaderToken {
        groups = Set.copyOf(groups);
    }

    @Override
    public @Nullable Object getPrincipal() {
        return user;
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }
}
