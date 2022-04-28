/*
 * Copyright (c) 2016 - 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test of StandaloneCommandLineInterface (and its dependencies; incl. real DB).
 *
 * @author Michael Vorburger.ch
 */
public class StandaloneCommandLineInterfaceTest {
    private static final String DIR = "target/" + StandaloneCommandLineInterfaceTest.class.getSimpleName();

    StandaloneCommandLineInterface cli;

    @Before
    public void before() throws Exception {
        FilesUtils.delete(DIR);
        cli = new StandaloneCommandLineInterface(new File(DIR));
    }

    @Test
    public void testInitialEmptyDatabase() throws Exception {
        assertEquals(List.of(), cli.getAllUserNames());
    }

    @Test
    public void testCreateNewUserAndSetPasswordAndDelete() throws Exception {
        assertEquals(List.of(), cli.getAllUserNames());
        assertFalse(cli.checkUserPassword("duh", "dah"));

        cli.createNewUser("test", "testpassword", false);
        assertThat(cli.getAllUserNames(), hasSize(1));
        assertEquals("test", cli.getAllUserNames().get(0));
        assertTrue(cli.checkUserPassword("test", "testpassword"));

        assertTrue(cli.resetPassword("test", "anothertestpassword"));
        assertFalse(cli.checkUserPassword("test", "testpassword"));
        assertTrue(cli.checkUserPassword("test", "anothertestpassword"));

        assertTrue(cli.deleteUser("test"));
        assertEquals(List.of(), cli.getAllUserNames());
        assertFalse(cli.checkUserPassword("test", "anothertestpassword"));
    }

    @Test // https://bugs.opendaylight.org/show_bug.cgi?id=8157
    public void testCreateDeleteReCreateUserBug8157() throws Exception {
        cli.createNewUser("test", "testpassword", false);
        assertTrue(cli.deleteUser("test"));
        cli.createNewUser("test", "testpassword", false);
    }

    @Test
    public void testSetPasswordOnNonExistingUser() throws Exception {
        assertFalse(cli.resetPassword("noSuchUID", "..."));
    }
}
