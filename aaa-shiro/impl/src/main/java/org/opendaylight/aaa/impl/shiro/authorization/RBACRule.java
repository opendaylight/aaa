/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.authorization;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A container for RBAC Rules. An RBAC Rule is composed of a url pattern which
 * may contain asterisk characters (*), and a collection of roles. These are
 * represented in shiro.ini in the following format:
 * <code>urlPattern=roles[atLeastOneCommaSeperatedRole]</code>
 *
 * RBACRules are immutable; that is, you cannot change the url pattern or the
 * roles after creation. This is done for security purposes. RBACRules are
 * created through utilizing a static factory method:
 * <code>RBACRule.createRBACRule()</code>
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class RBACRule {

    private static final Logger LOG = LoggerFactory.getLogger(RBACRule.class);

    /**
     * a url pattern that can optional contain asterisk characters (*)
     */
    private String urlPattern;

    /**
     * a collection of role names, such as "admin" and "user"
     */
    private Collection<String> roles = new HashSet<String>();

    /**
     * Creates an RBAC Rule. Made private for static factory method.
     *
     * @param urlPattern
     *            Cannot be null or the empty string.
     * @param roles
     *            Must contain at least one role.
     * @throws NullPointerException
     *             if <code>urlPattern</code> or <code>roles</code> is null
     * @throws IllegalArgumentException
     *             if <code>urlPattern</code> is an empty string or
     *             <code>roles</code> is an empty collection.
     */
    private RBACRule(final String urlPattern, final Collection<String> roles)
            throws NullPointerException, IllegalArgumentException {

        this.setUrlPattern(urlPattern);
        this.setRoles(roles);
    }

    /**
     * The static factory method used to create RBACRules.
     *
     * @param urlPattern
     *            Cannot be null or the empty string.
     * @param roles
     *            Cannot be null or an emtpy collection.
     * @return An immutable RBACRule
     */
    public static RBACRule createAuthorizationRule(final String urlPattern,
            final Collection<String> roles) {

        RBACRule authorizationRule = null;
        try {
            authorizationRule = new RBACRule(urlPattern, roles);
        } catch (Exception e) {
            LOG.error("Cannot instantiate the AuthorizationRule", e);
        }
        return authorizationRule;
    }

    /**
     *
     * @return the urlPattern for the RBACRule
     */
    public String getUrlPattern() {
        return urlPattern;
    }

    /*
     * helper to ensure the url pattern is not the empty string
     */
    private static void checkUrlPatternLength(final String urlPattern)
            throws IllegalArgumentException {

        final String EXCEPTION_MESSAGE = "Empty String is not allowed for urlPattern";
        if (urlPattern.isEmpty()) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }
    }

    private void setUrlPattern(final String urlPattern) throws NullPointerException,
            IllegalArgumentException {

        Preconditions.checkNotNull(urlPattern);
        checkUrlPatternLength(urlPattern);
        this.urlPattern = urlPattern;
    }

    /**
     *
     * @return a copy of the rule, so any modifications to the returned
     *         reference do not affect the immutable <code>RBACRule</code>.
     */
    public Collection<String> getRoles() {
        // Returns a copy of the roles collection such that the original set
        // keeps
        // its contract of remaining immutable.
        //
        // Since this method is only called at shiro initialiation time,
        // memory consumption of creating a new set is a non-issue.
        return Sets.newHashSet(roles);
    }

    /*
     * check to ensure the roles collection is not empty
     */
    private static void checkRolesCollectionSize(final Collection<String> roles)
            throws IllegalArgumentException {

        final String EXCEPTION_MESSAGE = "roles must contain at least 1 role";
        if (roles.isEmpty()) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }
    }

    private void setRoles(final Collection<String> roles) throws NullPointerException,
            IllegalArgumentException {

        Preconditions.checkNotNull(roles);
        checkRolesCollectionSize(roles);
        this.roles = roles;
    }

    /**
     * Generates a string representation of the <code>RBACRule</code> roles in
     * shiro form.
     *
     * @return roles string representation in the form
     *         <code>roles[roleOne,roleTwo]</code>
     */
    public String getRolesInShiroFormat() {
        final String ROLES_STRING = "roles";
        return ROLES_STRING + Arrays.toString(roles.toArray());
    }

    /**
     * Generates the string representation of the <code>RBACRule</code> in shiro
     * form. For example: <code>urlPattern=roles[admin,user]</code>
     */
    @Override
    public String toString() {
        return String.format("%s=%s", urlPattern, getRolesInShiroFormat());
    }
}
