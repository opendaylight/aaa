/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.serializers;

import org.opendaylight.aaa.api.clustering.AAAByteArrayWrapper;
import org.opendaylight.aaa.api.clustering.AAAObjectEncoder;
import org.opendaylight.aaa.api.clustering.AAAObjectSerializer;
import org.opendaylight.aaa.api.model.Role;

/**
 * Created by saichler@gmail.com on 12/4/15.
 */
public class RoleSerializer implements AAAObjectSerializer<Role> {

    public RoleSerializer(){
        AAAObjectEncoder.addSerializer(Role.class,this);
    }

    @Override
    public void encode(Role role, AAAByteArrayWrapper w) {
        AAAObjectEncoder.encodeString(role.getRoleid(),w);
        AAAObjectEncoder.encodeString(role.getDomainid(),w);
        AAAObjectEncoder.encodeString(role.getName(),w);
        AAAObjectEncoder.encodeString(role.getDescription(),w);
    }

    @Override
    public Role decode(AAAByteArrayWrapper w) {
        Role role = new Role();
        role.setRoleid(AAAObjectEncoder.decodeString(w));
        role.setDomainid(AAAObjectEncoder.decodeString(w));
        role.setName(AAAObjectEncoder.decodeString(w));
        role.setDescription(AAAObjectEncoder.decodeString(w));
        return role;
    }
}
