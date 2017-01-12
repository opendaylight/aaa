/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cassandra.persistence;

/**
 *
 * @author saichler@gmail.com
 *
 */
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;

@Deprecated
public class RoleStore extends AbstractStore<Role,Roles>{
    public RoleStore(CassandraStore store) throws NoSuchMethodException {
       super(store,Role.class,Roles.class,"setRoles","setRoleid");
    }
}

