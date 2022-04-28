/*
 * Copyright (c) 2016 - 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test of RealMain (and its dependencies; incl. args parsing, real DB, etc.).
 * This intentionally only tests a very basic scenario end-to-end; more fine grained cases are covered in the
 * {@link StandaloneCommandLineInterfaceTest} and the {@link AbstractMainTest}.
 *
 * @author Michael Vorburger.ch
 */
public class MainIntegrationTest {
    private static final String DIR = "target/" + MainIntegrationTest.class.getSimpleName();

    @Test
    public void testCLI() throws Exception {
        FilesUtils.delete(DIR);
        assertEquals(0, new Main()
            .parseArguments(new String[] { "-X", "--dbd", DIR, "-a", "-nu", "newuser", "-p", "firstpass" }));
        assertEquals(0, new Main()
            .parseArguments(new String[] { "-X", "--dbd", DIR, "-vu", "newuser", "-p", "firstpass" }));
        assertEquals(-7, new Main()
            .parseArguments(new String[] { "-X", "--dbd", DIR, "-vu", "newuser", "-p", "wrongpass" }));
        assertEquals(0, new Main()
            .parseArguments(new String[] { "-X", "--dbd", DIR, "-cu", "newuser", "-p", "newpass" }));
        assertEquals(-7, new Main()
            .parseArguments(new String[] { "-X", "--dbd", DIR, "-vu", "newuser", "-p", "firstpass" }));
        assertEquals(0, new Main()
            .parseArguments(new String[] { "-X", "--dbd", DIR, "-vu", "newuser", "-p", "newpass" }));
        assertEquals(0, new Main()
            .parseArguments(new String[] { "-X", "--dbd", DIR, "-du", "newuser" }));
        assertEquals(-7, new Main()
            .parseArguments(new String[] { "-X", "--dbd", DIR, "-vu", "newuser", "-p", "newpass" }));
    }

    @Test
    public void testMismatchUsersPasswords() throws Exception {
        assertEquals(-3, new Main().parseArguments(new String[] { "-X", "--dbd", DIR,
            "-vu", "newuser1", "-p", "newpass1",
            "-vu", "newuser2" /* No 2nd -p */ }));
    }
}
