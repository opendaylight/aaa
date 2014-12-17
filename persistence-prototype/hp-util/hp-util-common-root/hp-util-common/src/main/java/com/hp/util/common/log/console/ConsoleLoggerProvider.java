/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.log.console;

import com.hp.util.common.log.Logger;
import com.hp.util.common.log.LoggerProvider;

/**
 * Logger Provider special case to avoid using null.
 * 
 * @param <C> Type of the component to get loggers for
 * @author Fabiel Zuniga
 */
public class ConsoleLoggerProvider<C> implements LoggerProvider<C> {

    private Logger logger;

    /**
     * Creates a {@link LoggerProvider}.
     */
    public ConsoleLoggerProvider() {
        this(false);
    }

    /**
     * Creates a {@link LoggerProvider}.
     * 
     * @param excludeLogProperties {@code true} to exclude log properties (like timestamp, type,
     *            etc) and just print the message
     */
    public ConsoleLoggerProvider(boolean excludeLogProperties) {
        this.logger = new ConsoleLogger(excludeLogProperties);
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public Logger getLogger(C component) {
        return this.logger;
    }
}
