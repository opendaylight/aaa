/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by saichler@gmail.com on 12/4/15.
 */
public class Logger {

    private static Object logger = null;
    private static final String LOGGER_FACTORY_CLASS_NAME = "org.slf4j.LoggerFactory";
    private static Method errorMethod = null;

    static {
        initializeLogger();
    }

    public static final void logError(String msg,Throwable e){
        if(logger==null){
            if(msg!=null){
                System.err.println(msg);
            }
            if(e != null){
                e.printStackTrace();
            }
        }else{
            try {
                errorMethod.invoke(logger,new Object[]{msg,e});
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (InvocationTargetException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static final void initializeLogger(){
        try {
            Class c = Logger.class.getClassLoader().loadClass(LOGGER_FACTORY_CLASS_NAME);
            Method getloggerMethod = c.getMethod("getLogger",new Class[]{Class.class});
            logger = getloggerMethod.invoke(null,new Object[]{Logger.class});
            errorMethod = logger.getClass().getMethod("error",new Class[]{String.class,Throwable.class});
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        }
    }
}
