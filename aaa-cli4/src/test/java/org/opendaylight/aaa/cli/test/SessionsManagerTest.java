/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.cli.SessionsManager;

/**
 * @author mserngawy
 *
 */
public class SessionsManagerTest {

    @Test
    public void testSessionManager() {
        SessionsManager sessionMngr = SessionsManager.getInstance();
        assertNotNull(sessionMngr);
        final String usrName = "foo";
        final User usr = new User();
        usr.setName(usrName);
        usr.setDomainid("fooDomain");
        usr.setPassword("fooPwd");
        sessionMngr.addUserSession(usrName, usr);
        final User authUsr = sessionMngr.getCurrentUser(usrName);
        assertNotNull(authUsr);
        assertEquals(usr, authUsr);
    }

}
