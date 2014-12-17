/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.log.voidcase;

import com.hp.util.common.log.Logger;

/**
 * Logger special case to avoid using null (Null Object Pattern).
 * 
 * @author Fabiel Zuniga
 */
public class VoidLogger implements Logger {
    /*
     * NOTE: There are several ways of implementing the singleton pattern, some of them more secure
     * than others guaranteeing that one and only one instance will exists in the system (taking
     * care of deserialization). However, the singleton pattern is used here to minimize the number
     * of instances of this class since all of them will behave the same. It is irrelevant if the
     * system ended up with more than one instance of this class.
     */
    private static Logger INSTANCE = new VoidLogger();

    private VoidLogger() {

    }

    /**
     * Gets the only instance of this class.
     * 
     * @return the only instance of this class
     */
    public static Logger getInstance() {
        return INSTANCE;
    }

    @Override
    public void error(String message) {
    }

    @Override
    public void error(String message, Throwable cause) {
    }

    @Override
    public void warning(String message) {
    }

    @Override
    public void warning(String message, Throwable cause) {
    }

    @Override
    public void info(String message) {
    }

    @Override
    public void info(String message, Throwable cause) {
    }

    @Override
    public void debug(String message) {
    }

    @Override
    public void debug(String message, Throwable cause) {
    }
}
