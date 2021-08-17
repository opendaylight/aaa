/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

import javax.naming.OperationNotSupportedException;

/**
 *  This class is a utility to construct the different elements keys for the different data stores.
 *  For not making mistakes around the code constructing an element key, this class standardize the
 *  way the key is constructed to be used by the different data stores.
 *
 *  @author - Sharon Aicler (saichler@cisco.com)
 */
public final class IDMStoreUtil {
    private IDMStoreUtil() {
        // Hidden on purpose
    }

    public static String createUserid(String username, String domainid) {
        return username + "@" + domainid;
    }

    public static String createRoleid(String rolename, String domainid) {
        return rolename + "@" + domainid;
    }

    public static String createGrantid(String userid, String domainid, String roleid) {
        return userid + "@" + roleid + "@" + domainid;
    }
}
