/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.felix.service.command.CommandSession;

/**
 * CliUtils has helper methods for CLI bundle.
 *
 * @author mserngawy
 *
 */
public class CliUtils {

    public static final String LOGIN_FAILED_MESS = "User does not exist OR user name and passsword are not correct";

    /**
     * Retrieve the password from the user.
     *
     * @param session
     *            command line session
     * @param pwdPrintStr
     *            label for enter password
     * @return the new written password
     * @throws Exception
     *             exception reading the password
     */
    public static String readPassword(final CommandSession session, final String pwdPrintStr) throws Exception {
        session.getConsole().println(pwdPrintStr);
        final InputStreamReader iStreamReader = new InputStreamReader(session.getKeyboard());
        final BufferedReader bReader = new BufferedReader(iStreamReader);
        final String pwd = bReader.readLine();
        return pwd;
    }
}
