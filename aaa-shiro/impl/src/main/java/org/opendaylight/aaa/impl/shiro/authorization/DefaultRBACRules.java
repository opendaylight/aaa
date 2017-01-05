/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.authorization;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;

/**
 * A singleton container of default authorization rules that are installed as
 * part of Shiro initialization. This class defines an immutable set of rules
 * that are needed to provide system-wide security. These include protecting
 * certain MD-SAL leaf nodes that contain AAA data from random access. This is
 * not a place to define your custom rule set; additional RBAC rules are
 * configured through the shiro initialization file:
 * <code>$KARAF_HOME/shiro.ini</code>
 *
 * An important distinction to consider is that Shiro URL rules work to protect
 * the system at the Web layer, and <code>AuthzDomDataBroker</code> works to
 * protect the system down further at the DOM layer.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class DefaultRBACRules {

    private static DefaultRBACRules instance;

    /**
     * a collection of the default security rules
     */
    private Collection<RBACRule> rbacRules = new HashSet<RBACRule>();

    /**
     * protects the AAA MD-SAL store by preventing access to the leaf nodes to
     * non-admin users.
     */
    private static final RBACRule PROTECT_AAA_MDSAL = RBACRule.createAuthorizationRule(
            "*/authorization/*", Sets.newHashSet("admin"));

    /*
     * private for singleton pattern
     */
    private DefaultRBACRules() {
        // rbacRules.add(PROTECT_AAA_MDSAL);
    }

    /**
     *
     * @return the container instance for the default RBAC Rules
     */
    public static final DefaultRBACRules getInstance() {
        if (null == instance) {
            instance = new DefaultRBACRules();
        }
        return instance;
    }

    /**
     *
     * @return a copy of the default rules, so any modifications to the returned
     *         reference do not affect the <code>DefaultRBACRules</code>.
     */
    public final Collection<RBACRule> getRBACRules() {
        // Returns a copy of the rbacRules set such that the original set keeps
        // its contract of remaining immutable. Calls to rbacRules.add() are
        // encapsulated solely in <code>DefaultRBACRules</code>.
        //
        // Since this method is only called at shiro initialiation time,
        // memory consumption of creating a new set is a non-issue.
        return Sets.newHashSet(rbacRules);
    }
}
