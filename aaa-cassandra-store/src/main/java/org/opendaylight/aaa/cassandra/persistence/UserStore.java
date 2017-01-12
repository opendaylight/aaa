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
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;

@Deprecated
public class UserStore extends AbstractStore<User,Users>{
    public UserStore(CassandraStore store) throws NoSuchMethodException {
        super(store,User.class, Users.class, "setUsers", "setUserid");
    }
}

