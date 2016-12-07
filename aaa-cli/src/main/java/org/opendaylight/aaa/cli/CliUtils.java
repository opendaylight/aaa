/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli;

import org.apache.felix.service.command.CommandSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by mserngawy on 2016-12-07.
 */
public class CliUtils {

    public static String readPassword(final CommandSession session, final String pwdPrintStr) throws Exception {
        session.getConsole().println(pwdPrintStr);
        final InputStreamReader iStreamReader = new InputStreamReader(session.getKeyboard());
        final BufferedReader bReader = new BufferedReader(iStreamReader);
        final String pwd = bReader.readLine();
        return pwd;
    }
}
