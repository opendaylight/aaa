package org.opendaylight.aaa.h2.persistence.serializers;

import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.cluster.AAAByteArrayWrapper;
import org.opendaylight.aaa.cluster.AAAObjectEncoder;
import org.opendaylight.aaa.cluster.AAAObjectSerializer;

/**
 * Created by root on 12/4/15.
 */
public class UserSerializer implements AAAObjectSerializer<User>{

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
