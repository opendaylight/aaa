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
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit Test of Main class with the argument parsing.
 *
 * @author Michael Vorburger
 */
public class TestableMainTest {

    @Test
    public void noArguments() throws Exception {
        TestableMain main = Mockito.spy(new TestableMain());
        assertThat(main.parseArguments(new String[] {})).isEqualTo(-1);
        Mockito.verify(main).printHelp();
    }

    @Test
    public void unrecognizedArgument() throws Exception {
        TestableMain main = Mockito.spy(new TestableMain());
        assertThat(main.parseArguments(new String[] { "saywhat" })).isEqualTo(-1);
        Mockito.verify(main).unrecognizedOptions(Collections.singletonList("saywhat"));
        Mockito.verify(main).printHelp();
    }

    /**
     * Verify that allowsUnrecognizedOptions() is used, and "bad" arguments
     * print help message instead of causing an UnrecognizedOptionException.
     */
    @Test
    public void parsingError() throws Exception {
        TestableMain main = Mockito.spy(new TestableMain());
        assertThat(main.parseArguments(new String[] { "-d" })).isEqualTo(-1);
        Mockito.verify(main).printHelp();
    }

    @Test
    public void exceptionWithoutX() throws Exception {
        TestableMain main = new TestableMain() {
            @Override
            protected void printHelp() throws IOException {
                throw new IOException("boum");
            }
        };
        assertThat(main.parseArguments(new String[] { "-?" })).isEqualTo(-2);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionWithX() throws Exception {
        TestableMain main = new TestableMain() {
            @Override
            protected void printHelp() throws IOException {
                throw new IllegalStateException("boum");
            }
        };
        assertThat(main.parseArguments(new String[] { "-hX" })).isEqualTo(-2);
    }

    @Test
    public void onlyAUser() throws Exception {
        assertThat(new TestableMain().parseArguments(new String[] { "-X", "--nu", "admin" })).isEqualTo(-3);
    }

    @Test
    public void onlyTwoUsers() throws Exception {
        assertThat(new TestableMain().parseArguments(new String[] { "-X", "-cu", "admin", "-cu", "auser" }))
                .isEqualTo(-3);
    }

    @Test
    public void userOptionWithoutArgument() throws Exception {
        assertThat(new TestableMain().parseArguments(new String[] { "-nu" })).isEqualTo(-2);
    }

    @Test
    public void ifPasswordsThenEitherCreateOrChangeUser() throws Exception {
        TestableMain main = new TestableMain();
        assertThat(main.parseArguments(new String[] { "-X", "-p", "newpass" })).isEqualTo(-6);
    }

    @Test
    public void changeUserAndPassword() throws Exception {
        TestableMain main = Mockito.spy(new TestableMain());
        assertThat(main.parseArguments(new String[] { "-X", "-cu", "admin", "-p", "newpass" })).isEqualTo(0);
        Mockito.verify(main).setDbDirectory(new File("."));
        Mockito.verify(main).resetPasswords(Collections.singletonList("admin"), Collections.singletonList("newpass"));
    }

    @Test
    public void addNewUser() throws Exception {
        TestableMain main = Mockito.spy(new TestableMain());
        assertThat(main.parseArguments(new String[] { "-X", "-nu", "admin", "-p", "newpass" })).isEqualTo(0);
        Mockito.verify(main).setDbDirectory(new File("."));
        Mockito.verify(main).addNewUsers(Collections.singletonList("admin"), Collections.singletonList("newpass"));
    }

    @Test
    public void changeUserAndPasswordInNonDefaultDatabase() throws Exception {
        TestableMain main = Mockito.spy(new TestableMain());
        assertThat(main.parseArguments(new String[] { "-X", "--dbd", "altDbDir", "-cu", "admin", "-p", "newpass" }))
                .isEqualTo(0);
        Mockito.verify(main).setDbDirectory(new File("altDbDir"));
        Mockito.verify(main).resetPasswords(Collections.singletonList("admin"), Collections.singletonList("newpass"));
    }

    @Test
    public void changeTwoUsersAndPasswords() throws Exception {
        TestableMain main = Mockito.spy(new TestableMain());
        assertThat(main.parseArguments(
                new String[] { "-X", "-cu", "admin", "-p", "newpass1", "-cu", "auser", "-p", "newpass2" }))
                        .isEqualTo(0);
        Mockito.verify(main).resetPasswords(Arrays.asList("admin", "auser"), Arrays.asList("newpass1", "newpass2"));
    }

    @Test
    public void morePasswordsThanUsers() throws Exception {
        TestableMain main = new TestableMain();
        assertThat(main.parseArguments(new String[] { "-X", "-cu", "admin", "-p", "newpass1", "-cu", "auser", "-p",
            "newpass2", "-p", "newpass3" })).isEqualTo(-3);
    }

    @Test
    public void cantAddAndChangeUsersTogether() throws Exception {
        TestableMain main = new TestableMain();
        assertThat(main.parseArguments(new String[] { "-X", "-cu", "admin", "-nu", "admin2", "-p", "newpass1" }))
                .isEqualTo(-5);
    }

}
