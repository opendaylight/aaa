/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model;

import com.hp.util.common.log.LoggerProvider;

/**
 * Core module: Business logic and cross cutting logic.
 * 
 * @author Fabiel Zuniga
 */
public interface CoreModule {

    /**
     * Gets the application model or business logic API.
     *
     * @return the application model
     */
    public Model getModel();

    /**
     * Gets the logger provider.
     *
     * @return the logger provider
     */
    public LoggerProvider<Class<?>> getLoggerProvider();
}
