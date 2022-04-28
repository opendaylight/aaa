/*
 * Copyright (c) 2016 - 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.testutils.mockito.MoreAnswers;

/**
 * Unit Test of Main class with the argument parsing.
 *
 * @author Michael Vorburger.ch
 */
public class AbstractMainTest {
    private static AbstractMain mockedMain() {
        return mock(AbstractMain.class, MoreAnswers.realOrException());
    }

    @Test
    public void noArguments() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(-1, main.parseArguments(new String[] {}));
        verify(main).printHelp(any());
    }

    @Test
    public void unrecognizedArgument() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(-1, main.parseArguments(new String[] { "saywhat" }));
        verify(main).unrecognizedOptions(List.of("saywhat"));
        verify(main).printHelp(any());
    }

    /**
     * Verify that allowsUnrecognizedOptions() is used, and "bad" arguments
     * print help message instead of causing an UnrecognizedOptionException.
     */
    @Test
    public void parsingError() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(-1, main.parseArguments(new String[] { "-d" }));
        verify(main).printHelp(any());
    }

    @Test
    public void exceptionWithoutX() throws Exception {
        AbstractMain main = mockedMain();
        doThrow(new IllegalStateException()).when(main).printHelp(any());
        assertEquals(-2, main.parseArguments(new String[] { "-?" }));
    }

    @Test
    public void exceptionWithX() throws Exception {
        AbstractMain main = mockedMain();
        doThrow(new IllegalStateException()).when(main).printHelp(any());
        assertThrows(IllegalStateException.class, () -> main.parseArguments(new String[] { "-hX" }));
    }

    @Test
    public void onlyAUser() throws Exception {
        assertEquals(-3, mockedMain().parseArguments(new String[] { "-X", "--nu", "admin" }));
    }

    @Test
    public void onlyTwoUsers() throws Exception {
        assertEquals(-3, mockedMain().parseArguments(new String[] { "-X", "-cu", "admin", "-cu", "auser" }));
    }

    @Test
    public void userOptionWithoutArgument() throws Exception {
        assertEquals(-2, mockedMain().parseArguments(new String[] { "-nu" }));
    }

    @Test
    public void ifPasswordsThenEitherCreateOrChangeUser() throws Exception {
        assertEquals(-6, mockedMain().parseArguments(new String[] { "-X", "-p", "newpass" }));
    }

    @Test
    public void changeUserAndPassword() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(0, main.parseArguments(new String[] { "-X", "-cu", "user", "-p", "newpass" }));
        verify(main).setDbDirectory(new File("."));
        verify(main).resetPasswords(List.of("user"), List.of("newpass"));
    }

    @Test
    public void addNewUser() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(0, main.parseArguments(new String[] { "-X", "-nu", "user", "-p", "newpass" }));
        verify(main).setDbDirectory(new File("."));
        verify(main).addNewUsers(List.of("user"), List.of("newpass"), false);
    }

    @Test
    public void addNewAdminUser() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(0, main.parseArguments(new String[] { "-X", "-nu", "user", "-p", "newpass", "-a" }));
        verify(main).setDbDirectory(new File("."));
        verify(main).addNewUsers(List.of("user"), List.of("newpass"), true);
    }

    @Test
    public void changeUserAndPasswordInNonDefaultDatabase() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(0,
            main.parseArguments(new String[] { "-X", "--dbd", "altDbDir", "-cu", "user", "-p", "newpass" }));
        verify(main).setDbDirectory(new File("altDbDir"));
        verify(main).resetPasswords(List.of("user"), List.of("newpass"));
    }

    @Test
    public void changeTwoUsersAndPasswords() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(0, main.parseArguments(
                new String[] { "-X", "-cu", "user1", "-p", "newpass1", "-cu", "user2", "-p", "newpass2" }));
        verify(main).resetPasswords(List.of("user1", "user2"), List.of("newpass1", "newpass2"));
    }

    @Test
    public void morePasswordsThanUsers() throws Exception {
        assertEquals(-3, mockedMain().parseArguments(new String[] {
            "-X", "-cu", "admin", "-p", "newpass1", "-cu", "auser", "-p", "newpass2", "-p", "newpass3" }));
    }

    @Test
    public void cantAddAndChangeUsersTogether() throws Exception {
        assertEquals(-5,
            mockedMain().parseArguments(new String[] { "-X", "-cu", "admin", "-nu", "admin2", "-p", "newpass1" }));
    }

    @Test
    public void deleteSingleUser() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(0, main.parseArguments(new String[] { "-X", "-du", "duser" }));
        verify(main).setDbDirectory(new File("."));
        verify(main).deleteUsers(List.of("duser"));
    }

    @Test
    public void verify1User1Password() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(0, main.parseArguments(new String[] { "-X", "-vu", "user", "-p", "pass" }));
        verify(main).setDbDirectory(new File("."));
        verify(main).verifyUsers(List.of("user"), List.of("pass"));
    }

    @Test
    public void verify2User2Password() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(0, main.parseArguments(new String[] {
            "-X", "-vu", "user1", "-p", "pass1", "-vu", "user2", "-p", "pass2" }));
        verify(main).setDbDirectory(new File("."));
        verify(main).verifyUsers(List.of("user1", "user2"), List.of("pass1", "pass2"));
    }

    @Test
    public void verifyUserWithoutPassword() throws Exception {
        AbstractMain main = spy(AbstractMain.class);
        assertEquals(-3, main.parseArguments(new String[] { "-X", "-vu", "user" }));
    }
}
