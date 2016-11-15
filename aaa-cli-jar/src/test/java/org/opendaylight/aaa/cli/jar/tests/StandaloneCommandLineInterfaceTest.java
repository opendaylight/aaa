/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar.tests;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.cli.jar.StandaloneCommandLineInterface;

/**
 * Test for the standalone (JAR'd) CLI for AAA.
 *
 * @author Michael Vorburger
 */
public class StandaloneCommandLineInterfaceTest {

    private static final String DIR = "target/StandaloneCommandLineInterfaceTest";

    StandaloneCommandLineInterface cli;

    @Before
    public void before() throws IOException {
        delete(DIR);
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

        assertThat(cli.setPassword("test", "anothertestpassword")).isTrue();
    }

    @Test
    public void testSetPasswordOnNonExistingUser() throws Exception {
        assertThat(cli.setPassword("noSuchUID", "...")).isFalse();
    }

    private void delete(String directory) throws IOException {
        Path path = Paths.get(directory);
        if (!path.toFile().exists()) {
            return;
        }
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                file.toFile().delete();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                dir.toFile().delete();
                if (exc != null) {
                    throw exc;
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
