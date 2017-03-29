/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.testutils.mockito.MoreAnswers;

/**
 * Unit Test of Main class with the argument parsing.
 *
 * @author Michael Vorburger
 */
public class AbstractMainTest {

    private AbstractMain mockedMain() {
        return Mockito.mock(AbstractMain.class, MoreAnswers.realOrException());
    }

    @Test
    public void noArguments() throws Exception {
        AbstractMain main = Mockito.spy(AbstractMain.class);
        assertThat(main.parseArguments(new String[] {})).isEqualTo(-1);
        Mockito.verify(main).printHelp(any());
    }

    @Test
    public void unrecognizedArgument() throws Exception {
        AbstractMain main = Mockito.spy(AbstractMain.class);
        assertThat(main.parseArguments(new String[] { "saywhat" })).isEqualTo(-1);
        Mockito.verify(main).unrecognizedOptions(Collections.singletonList("saywhat"));
        Mockito.verify(main).printHelp(any());
    }

    /**
     * Verify that allowsUnrecognizedOptions() is used, and "bad" arguments
     * print help message instead of causing an UnrecognizedOptionException.
     */
    @Test
    public void parsingError() throws Exception {
        AbstractMain main = Mockito.spy(AbstractMain.class);
        assertThat(main.parseArguments(new String[] { "-d" })).isEqualTo(-1);
        Mockito.verify(main).printHelp(any());
    }

    @Test
    public void exceptionWithoutX() throws Exception {
        AbstractMain main = mockedMain();
        Mockito.doThrow(new IllegalStateException()).when(main).printHelp(any());
        assertThat(main.parseArguments(new String[] { "-?" })).isEqualTo(-2);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionWithX() throws Exception {
        AbstractMain main = mockedMain();
        Mockito.doThrow(new IllegalStateException()).when(main).printHelp(any());
        assertThat(main.parseArguments(new String[] { "-hX" })).isEqualTo(-2);
    }

    @Test
    public void onlyAUser() throws Exception {
        assertThat(mockedMain().parseArguments(new String[] { "-X", "--nu", "admin" })).isEqualTo(-3);
    }

    @Test
    public void onlyTwoUsers() throws Exception {
        assertThat(mockedMain().parseArguments(new String[] { "-X", "-cu", "admin", "-cu", "auser" }))
                .isEqualTo(-3);
    }

    @Test
    public void userOptionWithoutArgument() throws Exception {
        assertThat(mockedMain().parseArguments(new String[] { "-nu" })).isEqualTo(-2);
    }

    @Test
    public void ifPasswordsThenEitherCreateOrChangeUser() throws Exception {
        assertThat(mockedMain().parseArguments(new String[] { "-X", "-p", "newpass" })).isEqualTo(-6);
    }

    @Test
    public void changeUserAndPassword() throws Exception {
        AbstractMain main = Mockito.spy(AbstractMain.class);
        assertThat(main.parseArguments(new String[] { "-X", "-cu", "user", "-p", "newpass" })).isEqualTo(0);
        Mockito.verify(main).setDbDirectory(new File("."));
        Mockito.verify(main).resetPasswords(Collections.singletonList("user"), Collections.singletonList("newpass"));
    }

    @Test
    public void addNewUser() throws Exception {
        AbstractMain main = Mockito.spy(AbstractMain.class);
        assertThat(main.parseArguments(new String[] { "-X", "-nu", "user", "-p", "newpass" })).isEqualTo(0);
        Mockito.verify(main).setDbDirectory(new File("."));
        Mockito.verify(main).addNewUsers(
                Collections.singletonList("user"), Collections.singletonList("newpass"), false);
    }

    @Test
    public void addNewAdminUser() throws Exception {
        AbstractMain main = Mockito.spy(AbstractMain.class);
        assertThat(main.parseArguments(new String[] { "-X", "-nu", "user", "-p", "newpass", "-a" })).isEqualTo(0);
        Mockito.verify(main).setDbDirectory(new File("."));
        Mockito.verify(main).addNewUsers(Collections.singletonList("user"), Collections.singletonList("newpass"), true);
    }

    @Test
    public void changeUserAndPasswordInNonDefaultDatabase() throws Exception {
        AbstractMain main = Mockito.spy(AbstractMain.class);
        assertThat(main.parseArguments(new String[] { "-X", "--dbd", "altDbDir", "-cu", "user", "-p", "newpass" }))
                .isEqualTo(0);
        Mockito.verify(main).setDbDirectory(new File("altDbDir"));
        Mockito.verify(main).resetPasswords(Collections.singletonList("user"), Collections.singletonList("newpass"));
    }

    @Test
    public void changeTwoUsersAndPasswords() throws Exception {
        AbstractMain main = Mockito.spy(AbstractMain.class);
        assertThat(main.parseArguments(
                new String[] { "-X", "-cu", "user1", "-p", "newpass1", "-cu", "user2", "-p", "newpass2" }))
                        .isEqualTo(0);
        Mockito.verify(main).resetPasswords(Arrays.asList("user1", "user2"), Arrays.asList("newpass1", "newpass2"));
    }

    @Test
    public void morePasswordsThanUsers() throws Exception {
        assertThat(mockedMain().parseArguments(new String[] { "-X", "-cu", "admin", "-p", "newpass1", "-cu", "auser",
            "-p", "newpass2", "-p", "newpass3" })).isEqualTo(-3);
    }

    @Test
    public void cantAddAndChangeUsersTogether() throws Exception {
        assertThat(
                mockedMain().parseArguments(new String[] { "-X", "-cu", "admin", "-nu", "admin2", "-p", "newpass1" }))
                        .isEqualTo(-5);
    }

    @Test
    public void deleteSingleUser() throws Exception {
        AbstractMain main = Mockito.spy(AbstractMain.class);
        assertThat(main.parseArguments(new String[] { "-X", "-du", "duser" })).isEqualTo(0);
        Mockito.verify(main).setDbDirectory(new File("."));
        Mockito.verify(main).deleteUsers(Collections.singletonList("duser"));
    }
}
