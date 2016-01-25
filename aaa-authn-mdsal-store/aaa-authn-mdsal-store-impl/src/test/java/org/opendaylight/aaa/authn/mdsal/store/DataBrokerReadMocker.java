/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authn.mdsal.store;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBrokerReadMocker implements InvocationHandler {
    private Map<Method, List<StubContainer>> stubs = new HashMap<Method, List<StubContainer>>();
    private Class<?> mokingClass = null;

    @Override
    public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
        List<StubContainer> stList = stubs.get(arg1);
        if (stList != null) {
            for (StubContainer sc : stList) {
                if (sc.fitGeneric(arg2)) {
                    return sc.returnObject;
                }
            }
        }
        return null;
    }

    public DataBrokerReadMocker(Class<?> cls) {
        this.mokingClass = cls;
    }

    public static Object addMock(Class<?> cls) {
        return Proxy.newProxyInstance(cls.getClassLoader(), new Class[] { cls },
                new DataBrokerReadMocker(cls));
    }

    public static DataBrokerReadMocker getMocker(Object o) {
        return (DataBrokerReadMocker) Proxy.getInvocationHandler(o);
    }

    public static Method findMethod(Class<?> cls, String name, Object args[]) {
        Method methods[] = cls.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(name)) {
                if ((m.getParameterTypes() == null || m.getParameterTypes().length == 0)
                        && args == null) {
                    return m;
                }
                boolean match = true;
                for (int i = 0; i < m.getParameterTypes().length; i++) {
                    if (!m.getParameterTypes()[i].isAssignableFrom(args[i].getClass())) {
                        match = false;
                    }
                }
                if (match)
                    return m;
            }
        }
        return null;
    }

    public void addWhen(String methodName, Object[] args, Object returnThis)
            throws NoSuchMethodException, SecurityException {
        Method m = findMethod(this.mokingClass, methodName, args);
        if (m == null)
            throw new IllegalArgumentException("Unable to find method");
        StubContainer sc = new StubContainer(args, returnThis);
        List<StubContainer> lst = stubs.get(m);
        if (lst == null) {
            lst = new ArrayList<>();
        }
        lst.add(sc);
        stubs.put(m, lst);
    }

    private class StubContainer {
        private Class<?>[] parameters = null;
        private Class<?>[] generics = null;
        private Object args[] = null;
        private Object returnObject;

        public StubContainer(Object[] _args, Object ret) {
            this.args = _args;
            this.returnObject = ret;
        }

        public boolean fitGeneric(Object _args[]) {
            if (args == null && _args != null)
                return false;
            if (args != null && _args == null)
                return false;
            if (args == null && _args == null)
                return true;
            if (args.length != _args.length)
                return false;
            for (int i = 0; i < args.length; i++) {
                if (!args[i].equals(_args[i])) {
                    return false;
                }
            }
            return true;
        }
    }
}