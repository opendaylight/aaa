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
import org.opendaylight.aaa.api.model.Domain;

/**
 * Created by saichler@gmail.com on 12/4/15.
 */
public class DomainSerializer implements AAAObjectSerializer<Domain> {

    public DomainSerializer(){
        AAAObjectEncoder.addSerializer(Domain.class,this);
    }

    @Override
    public void encode(Domain domain, AAAByteArrayWrapper w) {
        AAAObjectEncoder.encodeString(domain.getDomainid(),w);
        AAAObjectEncoder.encodeString(domain.getName(),w);
        AAAObjectEncoder.encodeString(domain.getDescription(),w);
        AAAObjectEncoder.encodeBoolean(domain.isEnabled(),w);
    }

    @Override
    public Domain decode(AAAByteArrayWrapper w) {
        Domain domain = new Domain();
        domain.setDomainid(AAAObjectEncoder.decodeString(w));
        domain.setName(AAAObjectEncoder.decodeString(w));
        domain.setDescription(AAAObjectEncoder.decodeString(w));
        domain.setEnabled(AAAObjectEncoder.decodeBoolean(w));
        return domain;
    }
}
