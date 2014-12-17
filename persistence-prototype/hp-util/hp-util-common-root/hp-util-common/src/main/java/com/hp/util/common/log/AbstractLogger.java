/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.log;

import com.hp.util.common.log.Log.LogType;

/**
 * Abstract logger.
 * 
 * @author Fabiel Zuniga
 */
public abstract class AbstractLogger implements Logger {

    @Override
    public void error(String message) {
        writeLog(new Log(LogType.ERROR, message));
    }

    @Override
    public void error(String message, Throwable cause) {
        writeLog(new Log(LogType.ERROR, message, cause));
    }

    @Override
    public void warning(String message) {
        writeLog(new Log(LogType.WARNING, message));
    }

    @Override
    public void warning(String message, Throwable cause) {
        writeLog(new Log(LogType.WARNING, message, cause));
    }

    @Override
    public void info(String message) {
        writeLog(new Log(LogType.INFO, message));
    }

    @Override
    public void info(String message, Throwable cause) {
        writeLog(new Log(LogType.INFO, message, cause));
    }

    @Override
    public void debug(String message) {
        writeLog(new Log(LogType.DEBUG, message));
    }

    @Override
    public void debug(String message, Throwable cause) {
        writeLog(new Log(LogType.DEBUG, message, cause));
    }

    /**
     * Writes the log to the underlying output.
     * 
     * @param log log to write
     */
    protected abstract void writeLog(Log log);
}
