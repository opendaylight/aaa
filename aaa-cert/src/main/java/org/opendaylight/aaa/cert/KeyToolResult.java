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
import org.opendaylight.aaa.cert.api.AaaCertProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyToolResult extends JavaToolResult {

    private final Logger LOG = LoggerFactory.getLogger(KeyToolResult.class);
    private String errorMessage = "";
    private String message = "";

    public KeyToolResult() {

    }

    public void setErrorMessage(final InputStream inputStream) {
        final BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = input.readLine()) != null)
                sb.append(line);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        errorMessage = sb.toString();
    }

    public void setErrorMessage(String error) {
        errorMessage = error;
    }

    public void setMessage(final InputStream inputStream) {
        final BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = input.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        message = sb.toString();
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}