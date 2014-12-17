/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.hp.util.common.Interfaces;

/**
 * Dynamic Proxy.
 * <p>
 * Example:
 * <p>
 * <code>
 * <pre>
 * private &lt;T&gt; T prepareProxy(T myInterface) {
 *     T proxy = myInterface;
 *     proxy = DynamicProxy.create(proxy, new LatencyEmulatorProxy<T>());
 *     proxy = DynamicProxy.create(proxy, new SwingThreadMonitorProxy<T>());
 *     return proxy;
 *  }
 * </pre>
 * </code>
 * 
 * @param <T> the type of interface to handle invocations for
 * @author Fabiel Zuniga
 */
public class DynamicProxy<T> {

    private DynamicProxy() {

    }

    /**
     * Creates a dynamic proxy for all interfaces that the subject implements.
     * 
     * @param <P> type of the interface to create a proxy for (It MUST BE an interface)
     * @param <T> type of the subject to catch invocations for
     * @param subject subject to create a proxy for
     * @param proxy proxy
     * @return a dynamic proxy for the given {@code subject}
     */
    public static <P, T extends P> P create(T subject, Proxy<T> proxy) {
        @SuppressWarnings("unchecked")
        Class<T> subjectClass = (Class<T>) subject.getClass();
        Interfaces<T> interfaces = Interfaces.all(subjectClass);
        return create(subject, proxy, interfaces);
    }

    /**
     * Creates a dynamic proxy.
     * 
     * @param <P> type of the interface to create a proxy for (It MUST BE an interface)
     * @param <T> type of the subject to catch invocations for
     * @param subject subject to create a proxy for
     * @param proxy proxy
     * @param interfaces the set of interfaces for the proxy to intercept
     * @return a dynamic proxy for the given {@code subject}
     */
    public static <P, T extends P> P create(T subject, Proxy<T> proxy, Interfaces<T> interfaces) {
        InvocationHandler invocationHandler = new InvokationHandlerAdapter<T>(subject, proxy);
        Class<?>[] interfacesArray = interfaces.get().toArray(new Class<?>[0]);
        ClassLoader classLoader = subject.getClass().getClassLoader();
        @SuppressWarnings("unchecked")
        P dynamicProxy = (P) java.lang.reflect.Proxy.newProxyInstance(classLoader, interfacesArray, invocationHandler);
        return dynamicProxy;
    }

    /**
     * Creates a dynamic proxy for all interfaces that the subject implements.
     * 
     * @param <P> type of the interface to create a proxy for (It MUST BE an interface)
     * @param <T> type of the subject to catch invocations for
     * @param subjectClass subject' class to create a proxy for
     * @param proxy proxy
     * @return a dynamic proxy for the given {@code subject}
     */
    public static <P, T extends P> P create(Class<T> subjectClass, NonDelegatorProxy<T> proxy) {
        Interfaces<T> interfaces = Interfaces.all(subjectClass);
        return create(subjectClass, proxy, interfaces);
    }

    /**
     * Creates a dynamic proxy.
     * 
     * @param <P> type of the interface to create a proxy for (It MUST BE an interface)
     * @param <T> type of the subject to catch invocations for
     * @param subjectClass subject' class to create a proxy for
     * @param proxy proxy
     * @param interfaces the set of interfaces for the proxy to intercept
     * @return a dynamic proxy for the given {@code subject}
     */
    public static <P, T extends P> P create(Class<T> subjectClass, NonDelegatorProxy<T> proxy, Interfaces<T> interfaces) {
        InvocationHandler invocationHandler = new NonDelegatorInvokationHandlerAdapter<T>(subjectClass, interfaces,
                proxy);
        Class<?>[] interfacesArray = interfaces.get().toArray(new Class<?>[0]);
        ClassLoader classLoader = subjectClass.getClassLoader();
        @SuppressWarnings("unchecked")
        P dynamicProxy = (P) java.lang.reflect.Proxy.newProxyInstance(classLoader, interfacesArray, invocationHandler);
        return dynamicProxy;
    }

    private static class InvokationHandlerAdapter<T> implements InvocationHandler {

        private final T subject;
        private final Proxy<T> proxy;

        public InvokationHandlerAdapter(T subject, Proxy<T> proxy) {
            this.subject = subject;
            this.proxy = proxy;
        }

        @Override
        public Object invoke(Object dynamicProxy, Method method, Object[] args) throws Throwable {
            return this.proxy.invoke(this.subject, method, args);
        }
    }

    private static class NonDelegatorInvokationHandlerAdapter<T> implements InvocationHandler {

        private final Class<T> subjectClass;
        private final Interfaces<T> interfaces;
        private final NonDelegatorProxy<T> proxy;

        public NonDelegatorInvokationHandlerAdapter(Class<T> subjectClass, Interfaces<T> interfaces,
                NonDelegatorProxy<T> proxy) {
            this.subjectClass = subjectClass;
            this.interfaces = interfaces;
            this.proxy = proxy;
        }

        @Override
        public Object invoke(Object dynamicProxy, Method method, Object[] args) throws Throwable {
            return this.proxy.invoke(this.subjectClass, this.interfaces, method, args);
        }
    }
}
