/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.proxy;

import java.lang.reflect.Method;

import com.hp.util.common.Interfaces;

/**
 * Proxy that won't get a subject to delegate to, the proxy must dispatch the request.
 * 
 * @param <T> type of the subject to create a proxy for
 * @author Fabiel Zuniga
 */
public interface NonDelegatorProxy<T> {

    /**
     * Processes a method invocation on a proxy instance and returns the result.
     * 
     * @param subjectClass the suibject's class the proxy was created for
     * @param interfaces interfaces that the proxy was created for
     * @param method the method been invoked
     * @param args the method arguments
     * @return the invocation returned value
     * @throws Throwable if an error occurs
     */
    public Object invoke(Class<T> subjectClass, Interfaces<T> interfaces, Method method, Object[] args)
            throws Throwable;
}
