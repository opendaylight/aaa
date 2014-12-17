/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.Format;
import java.text.SimpleDateFormat;

import com.hp.util.common.type.Date;

/**
 * Log.
 * 
 * @author Fabiel Zuniga
 */
public class Log {
    private static final Format DEFAULT_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

    private Date timestamp;
    private LogType type;
    private StackTraceElement source;
    private String message;
    private Throwable cause;

    /**
     * Creates a new log.
     *
     * @param type Log type
     * @param message Log message
     */
    Log(LogType type, String message) {
        init(type, message, null);
    }

    /**
     * Creates a new log.
     *
     * @param type Log type
     * @param message Log message
     * @param cause Cause
     */
    Log(LogType type, String message, Throwable cause) {
        init(type, message, cause);
    }

    private void init(LogType aType, String aMessage, Throwable aCause) {
        // Initializes the log. init method is used so all calls have the same stack size. See
        // {@link Log#getSourceFromStackTrace()}.

        if (aType == null) {
            throw new NullPointerException("type cannot be null");
        }

        this.timestamp = Date.currentTime();
        this.type = aType;
        this.message = aMessage;
        this.cause = aCause;
        this.source = getSourceFromStackTrace();
    }

    private static StackTraceElement getSourceFromStackTrace() {
        // Returns the method that called the method that creates a new instance of Log
        final int sourceStackTraceIndex = 5;
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length >= sourceStackTraceIndex) {
            return stackTraceElements[sourceStackTraceIndex];
        }
        return null;
    }

    /**
     * Gets the log's timestamp.
     *
     * @return the timestamp
     */
    public Date getTimestamp() {
        return this.timestamp;
    }

    /**
     * Gets the log type.
     *
     * @return the type
     */
    public LogType getType() {
        return this.type;
    }

    /**
     * Gets the log source: Method that called the method that creates a new instance of Log.
     * Example:
     *
     * <pre>
     * private void source() {
     *     createLog();
     * }
     *
     * private Log createLog() {
     *     return new Log(....);
     * }
     * </pre>
     *
     * @return the source
     */
    public StackTraceElement getSource() {
        return this.source;
    }

    /**
     * Gets the log's message.
     *
     * @return the log's message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the cause.
     *
     * @return the cause
     */
    public Throwable getCause() {
        return this.cause;
    }

    @Override
    public String toString() {
        return toString(DEFAULT_FORMAT, "\t");
    }

    /**
     * Returns a string representation of this log.
     *
     * @param format Date format
     * @param separator Log field separator
     * @return a string representation of this log
     */
    public String toString(Format format, String separator) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        StringBuilder firstLogLine = new StringBuilder(128);
        firstLogLine.append(format.format(this.timestamp.toDate()));
        firstLogLine.append(separator);
        firstLogLine.append(this.type);
        firstLogLine.append(separator);
        // Example of StackTraceElement.toString():
        // com.hp.util.common.log.LogTest.testConstruction(LogTest.java:27)
        firstLogLine.append(this.source);
        firstLogLine.append(separator);
        firstLogLine.append(this.message);
        printStream.println(firstLogLine);


        if (this.cause != null) {
            this.cause.printStackTrace(printStream);
        }

        return outputStream.toString();
    }

    /**
     * Log type.
     */
    public static enum LogType {
        /** Error. */
        ERROR,
        /** Warning. */
        WARNING,
        /** Information. */
        INFO,
        /** Debug. */
        DEBUG
    }
}
