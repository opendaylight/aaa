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
import java.io.IOException;
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
    public void before() throws IOException {
        FilesUtils.delete(DIR);
        cli = new StandaloneCommandLineInterface(new File(DIR));
    }

    @Test
    public void testInitialEmptyDatabase() throws Exception {
        assertThat(cli.getAllUserNames()).isEmpty();
    }

    @Test
    public void testCreateNewUserAndSetPassword() throws Exception {
        cli.createNewUser("test", "testpassword");
        assertThat(cli.getAllUserNames()).hasSize(1);
        assertThat(cli.getAllUserNames().get(0)).isEqualTo("test");

        assertThat(cli.resetPassword("test", "anothertestpassword")).isTrue();
    }

    @Test
    public void testSetPasswordOnNonExistingUser() throws Exception {
        assertThat(cli.resetPassword("noSuchUID", "...")).isFalse();
    }

}
