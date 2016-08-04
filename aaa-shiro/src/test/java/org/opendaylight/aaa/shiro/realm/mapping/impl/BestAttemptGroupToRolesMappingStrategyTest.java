package org.opendaylight.aaa.shiro.realm.mapping.impl;

import static org.junit.Assert.*;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.aaa.shiro.realm.mapping.api.GroupsToRolesMappingStrategy;

/**
 * Test ODL's default groups->roles mapping strategy.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class BestAttemptGroupToRolesMappingStrategyTest {

    @Test
    public void mapGroupsToRoles() throws Exception {
        final GroupsToRolesMappingStrategy groupsToRolesMappingStrategy = new BestAttemptGroupToRolesMappingStrategy();
        final Collection<String> groups = Sets.newHashSet("person", "it");
        final String delimeter = ",";
        final Map<String, String> groupRolesMap = Maps.newHashMap();
        groupRolesMap.put("person", "user");
        groupRolesMap.put("it", "admin");
        groupRolesMap.put("noNeedToExist", "admin");

        final Map<String, Set<String>> result = groupsToRolesMappingStrategy.mapGroupsToRoles(
                groups, delimeter, groupRolesMap);
        assertTrue(result.keySet().contains("it"));
        assertTrue(result.keySet().contains("person"));
        assertTrue(result.get("it").contains("admin"));
        assertTrue(result.get("person").contains("user"));
        assertNull(result.get("noNeedToExist"));
    }

}