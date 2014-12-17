/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.log;

/**
 * Logger provider.
 * 
 * @param <C> Type of the component to provide loggers for
 * @author Fabiel Zuniga
 */
public interface LoggerProvider<C> {

    /**
     * Gets the default logger.
     *
     * @return default logger
     */
    public Logger getLogger();

    /**
     * Gets a logger for the given module.
     *
     * @param component component to get the logger for
     * @return A logger for the given component
     */
    public Logger getLogger(C component);
}
