/*
 * Copyright (c) 2016 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom slf4j <code>Appender</code> which stores <code>LoggingEvent</code>(s) in memory
 * for future retrieval.  This is useful from inside test resources.  This class is specified
 * within <code>logback-test.xml</code>.
 */
public class TestAppender extends AppenderBase<LoggingEvent> {

    /**
     * stores all log events in memory, instead of file.
     */
    private final List<LoggingEvent> events = new ArrayList<>();

    /**
     * Since junit maven & junit instantiate the logging appender (as provided
     * by logback-test.xml), singleton is not possible.  The next best thing is to track the
     * current instance so it can be retrieved by Test instances.
     */
    private static volatile TestAppender currentInstance;

    /**
     * keeps track of the current instance.
     */
    public TestAppender() {
        currentInstance = this;
    }

    @Override
    protected synchronized void append(final LoggingEvent loggingEvent) {
        events.add(loggingEvent);
    }

    /**
     * Extract the log.
     *
     * @return the in-memory representation of <code>LoggingEvent</code>(s)
     */
    public List<LoggingEvent> getEvents() {
        return events;
    }

    /**
     * A way to extract the appender from Test instances.
     *
     * @return <code>this</code>
     */
    public static TestAppender getCurrentInstance() {
        return currentInstance;
    }
}
