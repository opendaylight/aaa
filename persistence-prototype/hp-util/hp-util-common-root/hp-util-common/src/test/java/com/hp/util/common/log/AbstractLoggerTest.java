/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.log;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.common.log.Log.LogType;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class AbstractLoggerTest {

    @Test
    public void testError() {
        ConcreteLogger logger = new ConcreteLogger();
        String message = "message";
        logger.error(message);
        assertLog(logger.getLatestLog(), LogType.ERROR, message, null);
    }

    @Test
    public void testErrorWithCause() {
        ConcreteLogger logger = new ConcreteLogger();
        String message = "message";
        Exception cause = new Exception("cause");
        logger.error(message, cause);
        assertLog(logger.getLatestLog(), LogType.ERROR, message, cause);
    }

    @Test
    public void testWarning() {
        ConcreteLogger logger = new ConcreteLogger();
        String message = "message";
        logger.warning(message);
        assertLog(logger.getLatestLog(), LogType.WARNING, message, null);
    }

    @Test
    public void testWarningWithCause() {
        ConcreteLogger logger = new ConcreteLogger();
        String message = "message";
        Exception cause = new Exception("cause");
        logger.warning(message, cause);
        assertLog(logger.getLatestLog(), LogType.WARNING, message, cause);
    }

    @Test
    public void testInfo() {
        ConcreteLogger logger = new ConcreteLogger();
        String message = "message";
        logger.info(message);
        assertLog(logger.getLatestLog(), LogType.INFO, message, null);
    }

    @Test
    public void testInfoWithCause() {
        ConcreteLogger logger = new ConcreteLogger();
        String message = "message";
        Exception cause = new Exception("cause");
        logger.info(message, cause);
        assertLog(logger.getLatestLog(), LogType.INFO, message, cause);
    }

    @Test
    public void testDebug() {
        ConcreteLogger logger = new ConcreteLogger();
        String message = "message";
        logger.debug(message);
        assertLog(logger.getLatestLog(), LogType.DEBUG, message, null);
    }

    @Test
    public void testDebugWithCause() {
        ConcreteLogger logger = new ConcreteLogger();
        String message = "message";
        Exception cause = new Exception("cause");
        logger.debug(message, cause);
        assertLog(logger.getLatestLog(), LogType.DEBUG, message, cause);
    }

    private void assertLog(Log log, LogType expectedType, String expectedMessage, Throwable expectedCause) {
        // No need to assert source nor timestamp because LogTest already takes care of that.

        Assert.assertEquals(expectedType, log.getType());
        Assert.assertEquals(expectedMessage, log.getMessage());
        Assert.assertEquals(expectedCause, log.getCause());
    }

    private static class ConcreteLogger extends AbstractLogger {

        private Log latestLog;

        @Override
        protected void writeLog(Log log) {
            this.latestLog = log;
        }

        public Log getLatestLog() {
            return this.latestLog;
        }
    }
}
