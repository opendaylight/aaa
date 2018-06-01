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
import java.nio.charset.StandardCharsets;

/**
 * CliUtils has helper methods for CLI bundle.
 *
 * @author mserngawy
 *
 */
@SuppressWarnings("checkstyle:RegexpSingleLineJava")
public final class CliUtils {

    public static final String LOGIN_FAILED_MESS = "User does not exist OR user name and passsword are not correct";

    private CliUtils() {

    }

    /**
     * Retrieve the password from the user.
     *
     * @param pwdPrintStr
     *            label for enter password
     * @return the new written password
     * @throws Exception
     *             exception reading the password
     */
    public static String readPassword(final String pwdPrintStr) throws Exception {
        System.out.println(pwdPrintStr);
        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            return bReader.readLine();
        }
    }
}
