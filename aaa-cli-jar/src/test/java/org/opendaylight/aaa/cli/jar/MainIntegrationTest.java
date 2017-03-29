/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

/**
 * Test of RealMain (and its dependencies; incl. args parsing, real DB, etc.).
 * This intentionally only tests a very basic scenario end-to-end; more fine grained cases are covered in the
 * {@link StandaloneCommandLineInterfaceTest} and the {@link AbstractMainTest}.
 *
 * @author Michael Vorburger
 */
public class MainIntegrationTest {

    private static final String DIR = "target/" + MainIntegrationTest.class.getSimpleName();

    @Test
    public void testCLI() throws Exception {
        FilesUtils.delete(DIR);
        assertThat(new Main()
                .parseArguments(new String[] { "-X", "--dbd", DIR, "-a", "-nu", "newuser", "-p", "firstpass" }))
                .isEqualTo(0);
        assertThat(new Main()
                .parseArguments(new String[] { "-X", "--dbd", DIR, "-cu", "newuser", "-p", "newpass" }))
                .isEqualTo(0);
        assertThat(new Main()
                .parseArguments(new String[] { "-X", "--dbd", DIR, "-du", "newuser" }))
                .isEqualTo(0);
    }

}
