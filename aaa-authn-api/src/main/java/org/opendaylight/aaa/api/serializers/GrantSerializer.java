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
import org.opendaylight.aaa.api.model.Grant;

/**
 * Created by saichler@gmail.com on 12/4/15.
 */
public class GrantSerializer implements AAAObjectSerializer<Grant> {

    public GrantSerializer(){
        AAAObjectEncoder.addSerializer(Grant.class,this);
    }

    @Override
    public void encode(Grant grant, AAAByteArrayWrapper w) {
        AAAObjectEncoder.encodeString(grant.getGrantid(),w);
        AAAObjectEncoder.encodeString(grant.getDomainid(),w);
        AAAObjectEncoder.encodeString(grant.getRoleid(),w);
        AAAObjectEncoder.encodeString(grant.getUserid(),w);
    }

    @Override
    public Grant decode(AAAByteArrayWrapper w) {
        Grant grant = new Grant();
        grant.setGrantid(AAAObjectEncoder.decodeString(w));
        grant.setDomainid(AAAObjectEncoder.decodeString(w));
        grant.setRoleid(AAAObjectEncoder.decodeString(w));
        grant.setUserid(AAAObjectEncoder.decodeString(w));
        return grant;
    }
}
