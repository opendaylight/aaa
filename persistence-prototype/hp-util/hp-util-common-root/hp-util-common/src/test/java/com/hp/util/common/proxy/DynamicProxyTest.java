/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.proxy;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.common.Interfaces;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class DynamicProxyTest {

    @Test
    public void testCreateProxy() {
        Subject subject = new SubjectImpl();
        Assert.assertEquals("Hello World", subject.echo("Hello World"));
        subject = DynamicProxy.create(subject, new ProxyImpl());
        Assert.assertEquals("Proxied Hello World", subject.echo("Hello World"));
    }

    @Test
    public void testCreateNondelegableProxy() {
        Subject subject = DynamicProxy.create(Subject.class, new NonDelegatorProxyImpl());
        Assert.assertEquals("Proxy result", subject.echo("Hello World"));
    }

    private static interface Subject {
        public String echo(String input);
    }

    private static class SubjectImpl implements Subject {

        @Override
        public String echo(String input) {
            return input;
        }
    }

    private static class ProxyImpl implements Proxy<Subject> {

        @Override
        public Object invoke(Subject delegate, Method method, Object[] args) throws Throwable {
            // Alternative:
            // return "Proxied " + method.invoke(delegate, args);
            return "Proxied " + delegate.echo((String)args[0]);
        }
    }

    private static class NonDelegatorProxyImpl implements NonDelegatorProxy<Subject> {

        @Override
        public Object invoke(Class<Subject> subjectClass, Interfaces<Subject> interfaces, Method method, Object[] args)
                throws Throwable {
            return "Proxy result";
        }
    }
}
