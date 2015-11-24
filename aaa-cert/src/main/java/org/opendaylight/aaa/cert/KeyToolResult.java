/*
 * Copyright (c) 2015 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.maven.shared.utils.cli.javatool.JavaToolResult;

public class KeyToolResult extends JavaToolResult {

    private String errorMessage = "";

    public KeyToolResult() {

    }

    public void setErrorMessage(InputStream inputStream) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line = input.readLine()) != null)
            sb.append(line);
        errorMessage = sb.toString();
    }

    public void setErrorMessage(String error) {
        errorMessage = error;
    }

    public String getErrors() {
        return errorMessage;
    }
}