/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm.mapping.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.opendaylight.aaa.shiro.realm.mapping.api.GroupsToRolesMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps groups to roles if any association exists.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class BestAttemptGroupToRolesMappingStrategy implements GroupsToRolesMappingStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(BestAttemptGroupToRolesMappingStrategy.class);

    @Override
    public Map<String, Set<String>> mapGroupsToRoles(final Collection<String> groupNames, final String delimiter,
                                                     final Map<String, String> groupRolesMap) {

        final ImmutableMap.Builder<String, Set<String>> roleNamesBuilder = ImmutableMap.builder();

        if (groupRolesMap != null) {
            for (String groupName : groupNames) {
                final String roleNamesString = groupRolesMap.get(groupName);

                LOG.debug("association discovered: groupName={} and roleNamesString={}", groupName, roleNamesString);
                if (roleNamesString != null) {
                    final String[] roleNames = roleNamesString.split(delimiter);
                    final ImmutableSet.Builder<String> rolesSet = ImmutableSet.builder();
                    for (String roleName : roleNames) {
                        rolesSet.add(roleName);
                    }
                    roleNamesBuilder.put(groupName, rolesSet.build());
                }
            }
        } else {
            LOG.info("groupRolesMap was unspecified; directly mapping LDAP groups instead: {}", groupNames);
            for (String groupName : groupNames) {
                final ImmutableSet.Builder<String> rolesSet = ImmutableSet.builder();
                rolesSet.add(groupName);
                roleNamesBuilder.put(groupName, rolesSet.build());
            }
        }

        return roleNamesBuilder.build();

    }
}
