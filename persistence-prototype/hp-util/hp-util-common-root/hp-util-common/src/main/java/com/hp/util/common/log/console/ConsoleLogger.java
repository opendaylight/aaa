/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.log.console;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.hp.util.common.log.AbstractLogger;
import com.hp.util.common.log.Log;
import com.hp.util.common.log.Log.LogType;
import com.hp.util.common.log.Logger;

/**
 * Logger special case to avoid using null.
 * 
 * @author Fabiel Zuniga
 */
public class ConsoleLogger extends AbstractLogger {

    private final boolean excludeLogProperties;

    /**
     * Creates a {@link Logger}.
     */
    public ConsoleLogger() {
        this(false);
    }

    /**
     * Creates a {@link Logger}.
     * 
     * @param excludeLogProperties {@code true} to exclude log properties (like timestamp, type,
     *            etc) and just print the message
     */
    public ConsoleLogger(boolean excludeLogProperties) {
        this.excludeLogProperties = excludeLogProperties;
    }

    @Override
    protected void writeLog(Log log) {

        if (log.getType() == LogType.ERROR) {
            System.err.print(toPrintable(log));
        }
        else {
            System.out.print(toPrintable(log));
        }
    }

    private String toPrintable(Log log) {
        if (this.excludeLogProperties) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            printStream.println(log.getMessage());
            if (log.getCause() != null) {
                log.getCause().printStackTrace(printStream);
            }
            return outputStream.toString();
        }
        return log.toString();
    }
}
