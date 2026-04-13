/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.apache.shiro.authc.AuthenticationToken;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * AuthenticationToken used to pass information forwarded by oauth2-proxy. Contains no credential as we rely on user
 * being successfully authorized by identity provider server.
 *
 * @param groups groups or roles of the user
 * @param user user identifier
 */
@NonNullByDefault
public record Oauth2ProxyHeaderToken(List<String> groups, String user) implements AuthenticationToken {
    public Oauth2ProxyHeaderToken {
        groups = List.copyOf(requireNonNull(groups));
        requireNonNull(user);
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }
}
