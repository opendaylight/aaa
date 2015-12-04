package org.opendaylight.aaa.h2.persistence.serializers;

import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.cluster.AAAByteArrayWrapper;
import org.opendaylight.aaa.cluster.AAAObjectEncoder;
import org.opendaylight.aaa.cluster.AAAObjectSerializer;

/**
 * Created by root on 12/4/15.
 */
public class DomainSerializer implements AAAObjectSerializer<Domain>{
    @Override
    public void encode(Domain object, AAAByteArrayWrapper wrapper) {
        AAAObjectEncoder.encodeString(object.getDomainid(),wrapper);
        AAAObjectEncoder.encodeString(object.getName(),wrapper);
        AAAObjectEncoder.encodeString(object.getDescription(),wrapper);
        AAAObjectEncoder.encodeBoolean(object.isEnabled(),wrapper);
    }

    @Override
    public Domain decode(AAAByteArrayWrapper byteWrapper) {
        Domain domain = new Domain();
        domain.setDomainid(AAAObjectEncoder.decodeString(byteWrapper));
        domain.setName(AAAObjectEncoder.decodeString(byteWrapper));
        domain.setDescription(AAAObjectEncoder.decodeString(byteWrapper));
        domain.setEnabled(AAAObjectEncoder.decodeBoolean(byteWrapper));
        return domain;
    }
}
