package org.opendaylight.aaa.h2.persistence.serializers;

import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.cluster.AAAByteArrayWrapper;
import org.opendaylight.aaa.cluster.AAAObjectEncoder;
import org.opendaylight.aaa.cluster.AAAObjectSerializer;

/**
 * Created by root on 12/4/15.
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
