package org.opendaylight.aaa.h2.persistence.serializers;

import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.cluster.AAAByteArrayWrapper;
import org.opendaylight.aaa.cluster.AAAObjectEncoder;
import org.opendaylight.aaa.cluster.AAAObjectSerializer;

/**
 * Created by root on 12/4/15.
 */
public class RoleSerializer implements AAAObjectSerializer<Role>{

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
