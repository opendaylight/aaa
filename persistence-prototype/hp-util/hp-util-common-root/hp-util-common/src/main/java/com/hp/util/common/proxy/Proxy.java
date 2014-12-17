/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.proxy;

import java.lang.reflect.Method;

/**
 * Proxy.
 * 
 * @param <T> type of the subject or delegate to create a proxy for
 * @author Fabiel Zuniga
 */
public interface Proxy<T> {

    /**
     * Processes a method invocation on a proxy instance and returns the result.
     * 
     * @param subject subject or delegate for which the proxy was created for
     * @param method the method been invoked
     * @param args the method arguments
     * @return the invocation returned value
     * @throws Throwable if an error occurs
     */
    public Object invoke(T subject, Method method, Object[] args) throws Throwable;
}
