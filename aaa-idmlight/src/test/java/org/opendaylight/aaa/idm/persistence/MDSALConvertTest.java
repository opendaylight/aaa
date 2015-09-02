package org.opendaylight.aaa.idm.persistence;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.aaa.idm.model.Domain;

public class MDSALConvertTest {
    @Test
    public void testConvertDomain(){
        Domain d = new Domain();
        d.setDescription("hello");
        //d.setDomainid(54545);
        d.setEnabled(true);
        d.setName("Hello");
        org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.Domain mdsalDomain = d.toMDSAL();
        Assert.assertNotNull(mdsalDomain);
        Domain d2 = Domain.toObject(mdsalDomain);
        Assert.assertNotNull(d2);
        Assert.assertEquals(d, d2);
    }
}
