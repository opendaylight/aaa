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
 */
public interface IdMService {

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
     * @param domainName
     *            name of domain
     * @return list of roles
     */
    List<String> listRoles(String userId, String domainName);

    /**
     * List all user IDs.
     *
     * @return list of all user IDs
     * @throws IDMStoreException
     *             if the user IDs could not be read from an IIDMStore
     */
    List<String> listUserIDs() throws IDMStoreException;
}
