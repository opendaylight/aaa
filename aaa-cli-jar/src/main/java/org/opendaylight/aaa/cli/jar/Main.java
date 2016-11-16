/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.opendaylight.aaa.api.IDMStoreException;

/**
 * TestableMain which actually does something real.
 *
 * @author Michael Vorburger
 */
@SuppressWarnings("checkstyle:RegexpSingleLineJava") // allow System.out / System.err here..
public class Main extends AbstractMain {

    private StandaloneCommandLineInterface cli;

    @SuppressWarnings("checkstyle:IllegalThrows")
    public static void main(String[] args) throws Exception {
        System.exit(new Main().parseArguments(args));
    }

    @Override
    protected void setDbDirectory(File dbDirectory) throws IOException {
        cli = new StandaloneCommandLineInterface(dbDirectory);
    }

    @Override
    protected void listUsers() throws IDMStoreException {
        System.out.println("User names:");
        List<String> userNames = cli.getAllUserNames();
        for (String userName : userNames) {
            System.out.println(userName);
        }
    }

    @Override
    protected int resetPasswords(List<String> userNames, List<String> passwords) throws IDMStoreException {
        for (int i = 0; i < userNames.size(); i++) {
            String userName = userNames.get(i);
            String newPassword = passwords.get(i);
            boolean isSuccess = cli.resetPassword(userName, newPassword);
            if (isSuccess) {
                // Output text shamelessly copy/pasted from org.opendaylight.aaa.cli.ChangeUserPassword
                System.out.println(userName + "'s password has been changed");
            } else {
                System.err.println("User does not exist: " + userName);
                return -4;
            }
        }
        return 0;
    }

    @Override
    protected int addNewUsers(List<String> userNames, List<String> passwords) throws IDMStoreException {
        for (int i = 0; i < userNames.size(); i++) {
            String userName = userNames.get(i);
            String newPassword = passwords.get(i);
            cli.createNewUser(userName, newPassword);
            System.out.println("New user created: " + userName);
        }
        return 0;
    }

}
