/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.log.voidcase;

import com.hp.util.common.log.Logger;
import com.hp.util.common.log.LoggerProvider;

/**
 * Logger Provider special case to avoid using null (Null Object Pattern).
 * 
 * @param <C> Type of the component to get loggers for
 * @author Fabiel Zuniga
 */
public class VoidLoggerProvider<C> implements LoggerProvider<C> {
    /*
     * NOTE: There are several ways of implementing the singleton pattern, some of them more secure
     * than others guaranteeing that one and only one instance will exists in the system (taking
     * care of deserialization). However, the singleton pattern is used here to minimize the number
     * of instances of this class since all of them will behave the same. It is irrelevant if the
     * system ended up with more than one instance of this class.
     */
    @SuppressWarnings("rawtypes")
    private static final LoggerProvider INSTANCE = new VoidLoggerProvider();

    private VoidLoggerProvider() {

    }

    /**
     * Gets an instance of {@link LoggerProvider}.
     * 
     * @return instance of {@link LoggerProvider}
     */
    @SuppressWarnings("unchecked")
    public static <C> LoggerProvider<C> getInstance() {
        return INSTANCE;
    }

    @Override
    public Logger getLogger() {
        return VoidLogger.getInstance();
    }

    @Override
    public Logger getLogger(C component) {
        return VoidLogger.getInstance();
    }
}
