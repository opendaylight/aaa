/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.log;

import org.junit.Assert;
import org.junit.Test;

import com.hp.util.common.log.Log.LogType;
import com.hp.util.common.type.Date;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class LogTest {

    @Test
    public void testConstruction() {
        Exception cause = new Exception("cause");
        Log log = createLog(LogType.INFO, "log message", cause);

        Date timestamp = log.getTimestamp();
        Assert.assertNotNull(timestamp);
        Assert.assertTrue(timestamp.compareTo(Date.currentTime()) <= 0);
        Assert.assertTrue(Math.abs(timestamp.getTime() - Date.currentTime().getTime()) < 100);

        Assert.assertEquals(LogType.INFO, log.getType());
        Assert.assertEquals("log message", log.getMessage());
        Assert.assertSame(cause, log.getCause());

        StackTraceElement source = log.getSource();
        Assert.assertEquals(LogTest.class.getName(), source.getClassName());
        Assert.assertEquals("testConstruction", source.getMethodName());

        log = createLog(LogType.INFO, "log message");
        Assert.assertNull(log.getCause());
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidConstruction() {
        LogType invalidType = null;
        String validMessage = null;
        Exception validCause = null;

        createLog(invalidType, validMessage, validCause);
    }

    @Test
    public void testToString() {
        Log log = createLog(LogType.INFO, "log message", new Exception("cause"));
        Assert.assertFalse(log.toString().isEmpty());
    }

    private Log createLog(LogType type, String message, Throwable cause) {
        return new Log(type, message, cause);
    }

    private Log createLog(LogType type, String message) {
        return new Log(type, message);
    }
}
