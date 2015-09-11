/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api;

import java.util.List;

/**
 * A service to provide identity information.
 *
 * @author liemmn
 *
 */
public interface IdMService {
    /**
     * Retrieve the userId, given the userName, or null if no user by that name
     * exists.
     *
     * @param userName
     *            user name
     * @return user id or null if no user by that name exists
     */
    String getUserId(String userName);

    /**
     * List all domains that the given user has at least one role on.
     *
     * @param userId
     *            id of user
     * @return list of all domains that the given user has access to
     */
    List<String> listDomains(String userId);

    /**
     * List all roles that the given user has on the given domain.
     *
     * @param userId
     *            id of user
     * @param domain
     *            domain
     * @return list of roles
     */
    List<String> listRoles(String userId, String domain);
}
