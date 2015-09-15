package org.opendaylight.aaa.shiroact;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opendaylight.aaa.shiro.ServiceProxy;

public class ActivatorTest {

    @Test
    public void testActivatorEnablesServiceProxy() throws Exception {
        // should toggle the ServiceProxy enable status to true
        new Activator().init(null, null);;
        assertTrue(ServiceProxy.getInstance().getEnabled(null));
    }

}
