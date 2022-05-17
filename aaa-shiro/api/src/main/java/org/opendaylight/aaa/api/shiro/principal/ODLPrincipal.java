/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api.shiro.principal;

import java.security.Principal;
import java.util.Set;

/**
 * Principal for authentication.
 */
public interface ODLPrincipal extends Principal {

    /**
     * Extract username that is making the request.
     *
     * @return the requesting username
     */
    String getUsername();

    /**
     * Extract the domain that is making the request.
     *
     * @return the domain for the requesting username
     */
    String getDomain();

    /**
     * The user id for the user making the request, which is unique.
     *
     * @return the user id in the form username@domain
     */
    String getUserId();

    /**
     * The roles granted to the user making the request.
     *
     * @return roles associated with the user making the request.
     */
    Set<String> getRoles();
}
