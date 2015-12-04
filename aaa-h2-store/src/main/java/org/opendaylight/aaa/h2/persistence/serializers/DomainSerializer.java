package org.opendaylight.aaa.h2.persistence.serializers;

import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.cluster.AAAByteArrayWrapper;
import org.opendaylight.aaa.cluster.AAAObjectEncoder;
import org.opendaylight.aaa.cluster.AAAObjectSerializer;

/**
 * Created by root on 12/4/15.
 */
public class DomainSerializer implements AAAObjectSerializer<Domain>{

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
