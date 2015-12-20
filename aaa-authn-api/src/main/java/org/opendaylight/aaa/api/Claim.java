/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api;

import java.util.Set;

/**
 * A claim typically provided by an identity provider after validating the
 * needed identity and credentials.
 *
 * @author liemmn
 *
 */
public interface Claim {
    /**
     * Get the id of the authorized client. If the id is an empty string, it
     * means that the client is anonymous.
     *
     * @return id of the authorized client, or empty string if anonymous
     */
    String clientId();

    /**
     * Get the user id. User IDs are system-created.
     *
     * @return unique user id
     */
    String userId();

    /**
     * Get the user name. User names are externally created.
     *
     * @return unique user name
     */
    String user();

    /**
     * Get the fully-qualified domain name. Domain names are externally created.
     *
     * @return unique domain name, or empty string for a claim tied to no domain
     */
    String domain();

    /**
     * Get a set of user roles. Roles are externally created.
     *
     * @return set of user roles
     */
    Set<String> roles();
}