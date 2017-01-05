/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm.mapping.api;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Strategy to convert LDAP extracted groups/attributes to ODL roles.
 */
public interface GroupsToRolesMappingStrategy {

    /**
     * Convert LDAP groups to ODL roles.
     *
     * @param groups A collection of String groups extracted from making an LDAP query.
     * @param delimeter A separator to allow multiple target roles.
     * @param groupRolesMap The association between groups to roles
     * @return A <code>non-null</code> map with group as the key and roles as the value
     */
    Map<String, Set<String>> mapGroupsToRoles(final Collection<String> groups, final String delimeter,
                                              final Map<String, String> groupRolesMap);
}
