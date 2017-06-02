/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.cli.AaaCliAbstractCommand;
import org.opendaylight.aaa.cli.SessionsManager;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Base class for all the Test Cases testing the CLI commands.
 *
 * @author mserngawy
 *
 */
@RunWith(PowerMockRunner.class)
public class AaaCliAbstractCommandTest {

    class TestAaaCliAbstractCommand extends AaaCliAbstractCommand {

        TestAaaCliAbstractCommand(IIDMStore identityStore) {
            super(identityStore);
        }

        @Override
        protected Object doExecute() throws Exception {
            return super.doExecute();
        }
    }

    private static final String AUTH_USER_NAME = "foo";
    final User usr = new User();
    @Mock
    private TestAaaCliAbstractCommand testCmd;

    @Before
    public void setUp() throws Exception {
        testCmd = PowerMockito.mock(TestAaaCliAbstractCommand.class, Mockito.CALLS_REAL_METHODS);
        MemberModifier.field(TestAaaCliAbstractCommand.class, "authUser").set(testCmd, AUTH_USER_NAME);

        final String usrName = "foo";
        usr.setName(usrName);
        usr.setDomainid("fooDomain");
        usr.setPassword("fooPwd");
        SessionsManager sessionMngr = SessionsManager.getInstance();
        sessionMngr.addUserSession(usrName, usr);
    }

    @Test
    public void testDoExecute() throws Exception {
        User authUsr = (User) testCmd.doExecute();
        assertNotNull(authUsr);
        assertEquals(authUsr, usr);
    }
}
