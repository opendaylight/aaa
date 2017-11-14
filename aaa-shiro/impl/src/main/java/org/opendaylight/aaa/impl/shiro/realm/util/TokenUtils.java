/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.impl.shiro.realm.util;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * Utilities for manipulating <code>AuthenticationToken</code> instances from Shiro.
 */
public class TokenUtils {
    /**
     * extract the username from an <code>AuthenticationToken</code>
     *
     * @param authenticationToken
     * @return
     * @throws ClassCastException
     * @throws NullPointerException
     */
    public static String extractUsername(final AuthenticationToken authenticationToken)
            throws ClassCastException, NullPointerException {

        return (String) authenticationToken.getPrincipal();
    }

    /**
     * extract the password from an <code>AuthenticationToken</code>
     *
     * @param authenticationToken
     * @return
     * @throws ClassCastException
     * @throws NullPointerException
     */
    public static String extractPassword(final AuthenticationToken authenticationToken)
            throws ClassCastException, NullPointerException {

        final UsernamePasswordToken upt = (UsernamePasswordToken) authenticationToken;
        return new String(upt.getPassword());
    }
}
