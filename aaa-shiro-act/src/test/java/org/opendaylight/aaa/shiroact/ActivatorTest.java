/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiroact;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opendaylight.aaa.shiro.ServiceProxy;

public class ActivatorTest {

    @Test
    public void testActivatorEnablesServiceProxy() throws Exception {
        // should toggle the ServiceProxy enable status to true
        new Activator().start(null);;
        assertTrue(ServiceProxy.getInstance().getEnabled(null));
    }

}
