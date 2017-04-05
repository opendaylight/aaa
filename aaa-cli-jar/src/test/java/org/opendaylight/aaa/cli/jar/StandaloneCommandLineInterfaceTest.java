/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import org.junit.Before;
import org.junit.Test;

/**
 * Test of StandaloneCommandLineInterface (and its dependencies; incl. real DB).
 *
 * @author Michael Vorburger
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
        assertThat(cli.getAllUserNames()).isEmpty();
    }

    @Test
    public void testCreateNewUserAndSetPasswordAndDelete() throws Exception {
        assertThat(cli.getAllUserNames()).isEmpty();
        cli.createNewUser("test", "testpassword", false);
        assertThat(cli.getAllUserNames()).hasSize(1);
        assertThat(cli.getAllUserNames().get(0)).isEqualTo("test");

        assertThat(cli.resetPassword("test", "anothertestpassword")).isTrue();

        assertThat(cli.deleteUser("test")).isTrue();
        assertThat(cli.getAllUserNames()).hasSize(0);
    }

    @Test // https://bugs.opendaylight.org/show_bug.cgi?id=8157
    public void testCreateDeleteReCreateUserBug8157() throws Exception {
        cli.createNewUser("test", "testpassword", false);
        assertThat(cli.deleteUser("test")).isTrue();
        cli.createNewUser("test", "testpassword", false);
    }

    @Test
    public void testSetPasswordOnNonExistingUser() throws Exception {
        assertThat(cli.resetPassword("noSuchUID", "...")).isFalse();
    }

}
