/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.DomainBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;

public abstract class IDMObject2MDSAL {
    private static Map<Class<?>,ConvertionMethods> typesMethods = new HashMap<Class<?>,ConvertionMethods>();
    private static Object fromMDSALObject(Object mdsalObject,Class<?> type) throws Exception{
        Object result = type.newInstance();
        ConvertionMethods cm = typesMethods.get(type);
        if(cm==null){
            cm = new ConvertionMethods();
            typesMethods.put(type, cm);
            Method methods[] = type.getMethods();
            for(Method m:methods){
                if(m.getName().startsWith("set")){
                    cm.setMethods.add(m);
                    Method gm = null;
                    if(m.getParameterTypes()[0].equals(Boolean.class) || m.getParameterTypes()[0].equals(boolean.class))
                        gm = ((DataObject)mdsalObject).getImplementedInterface().getMethod("is"+m.getName().substring(3), (Class[])null);
                    else
                        gm = ((DataObject)mdsalObject).getImplementedInterface().getMethod("get"+m.getName().substring(3), (Class[])null);
                    cm.getMethods.put(m.getName(), gm);
                }
            }
        }
        for(Method m:cm.setMethods){
            try{
                m.invoke(result, new Object[]{cm.getMethods.get(m.getName()).invoke(mdsalObject, (Object[])null)});
            }catch(Exception err){
                err.printStackTrace();
            }
        }
        return result;
    }

    private static Object toMDSALObject(Object object,Class<?> mdSalBuilderType) throws Exception{
        Object result = mdSalBuilderType.newInstance();
        ConvertionMethods cm = typesMethods.get(mdSalBuilderType);
        if(cm==null){
            cm = new ConvertionMethods();
            typesMethods.put(mdSalBuilderType, cm);
            Method methods[] = mdSalBuilderType.getMethods();
            for(Method m:methods){
                if(m.getName().startsWith("set")){
                    try{
                        Method gm = null;
                        if(m.getParameterTypes()[0].equals(Boolean.class) || m.getParameterTypes()[0].equals(boolean.class))
                            gm = object.getClass().getMethod("is"+m.getName().substring(3), (Class[])null);
                        else
                            gm = object.getClass().getMethod("get"+m.getName().substring(3), (Class[])null);
                        cm.getMethods.put(m.getName(), gm);
                        cm.setMethods.add(m);
                    }catch(NoSuchMethodException err){}
                }
            }
            cm.builderMethod = mdSalBuilderType.getMethod("build",(Class[])null);
        }
        for(Method m:cm.setMethods){
            m.invoke(result, new Object[]{cm.getMethods.get(m.getName()).invoke(object, (Object[])null)});
        }

        return cm.builderMethod.invoke(result, (Object[])null);
    }

    private static class ConvertionMethods {
        private List<Method> setMethods = new ArrayList<Method>();
        private Map<String,Method> getMethods = new HashMap<String,Method>();
        private Method builderMethod = null;
    }

    //Convert Domain
    public static org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain toMDSALDomain(Domain domain){
        try{
            return (org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain)toMDSALObject(domain, DomainBuilder.class);
        }catch(Exception err){
            err.printStackTrace();
            return null;
        }
    }
    public static Domain toIDMDomain(org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain domain){
        try{
            return (Domain)fromMDSALObject(domain, Domain.class);
        }catch(Exception err){
            err.printStackTrace();
            return null;
        }
    }    
}
