/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli;

import java.util.List;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Role;

public class DataStoreUtils {

    public static final String getDomainId(IIDMStore identityStore, String domainName) throws IDMStoreException {
         List<Domain> domains = identityStore.getDomains().getDomains();
         for (Domain domain : domains) {
            if (domain.getName().equals(domainName)) {
                return domain.getDomainid();
            }
         }
         return null;
    }

    public static final String getRoleId(IIDMStore identityStore, String roleName) throws IDMStoreException {
        List<Role> roles = identityStore.getRoles().getRoles();
        for (Role role : roles) {
           if (role.getName().equals(roleName)) {
               return role.getRoleid();
           }
        }
        return null;
   }

}
