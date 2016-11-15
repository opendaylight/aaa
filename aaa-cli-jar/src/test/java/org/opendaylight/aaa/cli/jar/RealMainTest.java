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
 * {@link StandaloneCommandLineInterfaceTest} and the {@link TestableMainTest}.
 *
 * @author Michael Vorburger
 */
public class RealMainTest {

    private static final String DIR = "target/" + RealMainTest.class.getSimpleName();

    @Test
    public void testCLI() throws Exception {
        FilesUtils.delete(DIR);
        assertThat(new RealMain()
                .parseArguments(new String[] { "-X", "--dbd", DIR, "-nu", "newuser", "-p", "firstpass" }))
                .isEqualTo(0);
        assertThat(new RealMain()
                .parseArguments(new String[] { "-X", "--dbd", DIR, "-cu", "newuser", "-p", "newpass" }))
                .isEqualTo(0);
    }

}
