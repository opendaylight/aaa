/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.authorization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import org.junit.Test;

public class RBACRuleTest {

    private static final String BASIC_RBAC_RULE_URL_PATTERN = "/*";
    private static final Collection<String> BASIC_RBAC_RULE_ROLES = Sets.newHashSet("admin");
    private RBACRule basicRBACRule = RBACRule.createAuthorizationRule(BASIC_RBAC_RULE_URL_PATTERN,
            BASIC_RBAC_RULE_ROLES);

    private static final String COMPLEX_RBAC_RULE_URL_PATTERN = "/auth/v1/";
    private static final Collection<String> COMPLEX_RBAC_RULE_ROLES = Sets.newHashSet("admin",
            "user");
    private RBACRule complexRBACRule = RBACRule.createAuthorizationRule(
            COMPLEX_RBAC_RULE_URL_PATTERN, COMPLEX_RBAC_RULE_ROLES);

    @Test
    public void testCreateAuthorizationRule() {
        // positive test cases
        assertNotNull(RBACRule.createAuthorizationRule(BASIC_RBAC_RULE_URL_PATTERN,
                BASIC_RBAC_RULE_ROLES));
        assertNotNull(RBACRule.createAuthorizationRule(COMPLEX_RBAC_RULE_URL_PATTERN,
                COMPLEX_RBAC_RULE_ROLES));

        // negative test cases
        // both null
        assertNull(RBACRule.createAuthorizationRule(null, null));

        // url pattern is null
        assertNull(RBACRule.createAuthorizationRule(null, BASIC_RBAC_RULE_ROLES));
        // url pattern is empty string
        assertNull(RBACRule.createAuthorizationRule("", BASIC_RBAC_RULE_ROLES));

        // roles is null
        assertNull(RBACRule.createAuthorizationRule(BASIC_RBAC_RULE_URL_PATTERN, null));
        // roles is empty collection
        assertNull(RBACRule.createAuthorizationRule(COMPLEX_RBAC_RULE_URL_PATTERN,
                new HashSet<String>()));
    }

    @Test
    public void testGetUrlPattern() {
        assertEquals(BASIC_RBAC_RULE_URL_PATTERN, basicRBACRule.getUrlPattern());
        assertEquals(COMPLEX_RBAC_RULE_URL_PATTERN, complexRBACRule.getUrlPattern());
    }

    @Test
    public void testGetRoles() {
        assertTrue(BASIC_RBAC_RULE_ROLES.containsAll(basicRBACRule.getRoles()));
        basicRBACRule.getRoles().clear();
        // test that getRoles() produces a new object
        assertFalse(basicRBACRule.getRoles().isEmpty());
        assertTrue(basicRBACRule.getRoles().containsAll(BASIC_RBAC_RULE_ROLES));

        assertTrue(COMPLEX_RBAC_RULE_ROLES.containsAll(complexRBACRule.getRoles()));
        complexRBACRule.getRoles().add("newRole");
        // test that getRoles() produces a new object
        assertFalse(complexRBACRule.getRoles().contains("newRole"));
        assertTrue(complexRBACRule.getRoles().containsAll(COMPLEX_RBAC_RULE_ROLES));
    }

    @Test
    public void testGetRolesInShiroFormat() {
        final String BASIC_RBAC_RULE_EXPECTED_SHIRO_FORMAT = "roles[admin]";
        assertEquals(BASIC_RBAC_RULE_EXPECTED_SHIRO_FORMAT, basicRBACRule.getRolesInShiroFormat());

        // set ordering is not predictable, so both formats must be considered
        final String COMPLEX_RBAC_RULE_EXPECTED_SHIRO_FORMAT_1 = "roles[admin, user]";
        final String COMPLEX_RBAC_RULE_EXPECTED_SHIRO_FORMAT_2 = "roles[user, admin]";
        assertTrue(COMPLEX_RBAC_RULE_EXPECTED_SHIRO_FORMAT_1.equals(complexRBACRule
                .getRolesInShiroFormat())
                || COMPLEX_RBAC_RULE_EXPECTED_SHIRO_FORMAT_2.equals(complexRBACRule
                        .getRolesInShiroFormat()));
    }

    @Test
    public void testToString() {
        final String BASIC_RBAC_RULE_EXPECTED_SHIRO_FORMAT = "/*=roles[admin]";
        assertEquals(BASIC_RBAC_RULE_EXPECTED_SHIRO_FORMAT, basicRBACRule.toString());

        // set ordering is not predictable,s o both formats must be considered
        final String COMPLEX_RBAC_RULE_EXPECTED_SHIRO_FORMAT_1 = "/auth/v1/=roles[admin, user]";
        final String COMPLEX_RBAC_RULE_EXPECTED_SHIRO_FORMAT_2 = "/auth/v1/=roles[user, admin]";
        assertTrue(COMPLEX_RBAC_RULE_EXPECTED_SHIRO_FORMAT_1.equals(complexRBACRule.toString())
                || COMPLEX_RBAC_RULE_EXPECTED_SHIRO_FORMAT_2.equals(complexRBACRule.toString()));
    }

}
