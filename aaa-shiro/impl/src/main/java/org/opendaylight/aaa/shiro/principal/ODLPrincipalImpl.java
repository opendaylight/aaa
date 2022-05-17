/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.principal;

import java.util.Set;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;

/**
 * An ODL specific principal which stores some critical information about the user
 * making the auth request.
 */
public final class ODLPrincipalImpl implements ODLPrincipal {

    private final String username;
    private final String domain;
    private final String userId;
    private final Set<String> roles;

    private ODLPrincipalImpl(final String username, final String domain, final String userId, final Set<String> roles) {
        this.username = username;
        this.domain = domain;
        this.userId = userId;
        this.roles = roles;
    }

    /**
     * A static factory method to create <code>ODLPrincipal</code> instances.
     *
     * @param auth Contains identifying information for the particular request.
     * @return A Principal for the given session;  essentially a DTO.
     */
    public static ODLPrincipal createODLPrincipal(Authentication auth) {
        return createODLPrincipal(auth.user(), auth.domain(), auth.userId(), auth.roles());
    }

    /**
     * A static factory method to create <code>ODLPrincipal</code> instances.
     *
     * @param username The authenticated user
     * @param domain The domain <code>username</code> belongs to.
     * @param userId The unique key for <code>username</code>
     * @param roles The roles associated with <code>username</code>@<code>domain</code>
     * @return A Principal for the given session;  essentially a DTO.
     */
    public static ODLPrincipal createODLPrincipal(String username, String domain,
                                           String userId, Set<String> roles) {

        return new ODLPrincipalImpl(username, domain, userId, roles);
    }

    /**
     * A static factory method to create <code>ODLPrincipal</code> instances w/o roles.
     *
     * @param username The authenticated user
     * @param domain The domain <code>username</code> belongs to.
     * @param userId The unique key for <code>username</code>
     * @return A Principal for the given session;  essentially a DTO.
     */
    public static ODLPrincipal createODLPrincipal(String username, String domain,
                                                  String userId) {
        return ODLPrincipalImpl.createODLPrincipal(username, domain, userId, null);
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getDomain() {
        return this.domain;
    }

    @Override
    public String getUserId() {
        return this.userId;
    }

    @Override
    public Set<String> getRoles() {
        return this.roles;
    }

    @Override
    public String getName() {
        return getUserId();
    }
}
