/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.impl;

import com.hp.demo.plugable.persistence.model.CoreModule;
import com.hp.demo.plugable.persistence.model.Model;
import com.hp.util.common.log.LoggerProvider;
import com.hp.util.common.log.slf4j.Slf4jLoggerProvider;

/**
 * @author Fabiel Zuniga
 */
class CoreModuleImpl implements CoreModule {

    private final Model model;
    private final LoggerProvider<Class<?>> loggerProvider;

    public CoreModuleImpl() {
        this.loggerProvider = new Slf4jLoggerProvider();
        this.model = new ModelImpl(this.loggerProvider);
    }

    @Override
    public Model getModel() {
        return this.model;
    }

    @Override
    public LoggerProvider<Class<?>> getLoggerProvider() {
        return this.loggerProvider;
    }
}
