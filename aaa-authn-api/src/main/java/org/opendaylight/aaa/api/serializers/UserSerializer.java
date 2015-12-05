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
import org.opendaylight.aaa.api.model.User;

/**
 * Created by saichler@gmail.com on 12/4/15.
 */
public class UserSerializer implements AAAObjectSerializer<User> {

    public UserSerializer(){
        AAAObjectEncoder.addSerializer(User.class,this);
    }

    @Override
    public void encode(User user, AAAByteArrayWrapper w) {
        AAAObjectEncoder.encodeString(user.getUserid(),w);
        AAAObjectEncoder.encodeString(user.getDomainid(),w);
        AAAObjectEncoder.encodeString(user.getName(),w);
        AAAObjectEncoder.encodeString(user.getDescription(),w);
        AAAObjectEncoder.encodeString(user.getEmail(),w);
        AAAObjectEncoder.encodeString(user.getPassword(),w);
        AAAObjectEncoder.encodeString(user.getSalt(),w);
        AAAObjectEncoder.encodeBoolean(user.isEnabled(),w);
    }

    @Override
    public User decode(AAAByteArrayWrapper w) {
        User user = new User();
        user.setUserid(AAAObjectEncoder.decodeString(w));
        user.setDomainid(AAAObjectEncoder.decodeString(w));
        user.setName(AAAObjectEncoder.decodeString(w));
        user.setDescription(AAAObjectEncoder.decodeString(w));
        user.setEmail(AAAObjectEncoder.decodeString(w));
        user.setPassword(AAAObjectEncoder.decodeString(w));
        user.setSalt(AAAObjectEncoder.decodeString(w));
        user.setEnabled(AAAObjectEncoder.decodeBoolean(w));
        return user;
    }
}
