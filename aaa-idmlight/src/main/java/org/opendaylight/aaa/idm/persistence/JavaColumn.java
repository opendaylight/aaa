/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.aaa.idm.persistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
 * @Author - Sharon Aicler (saichler@cisco.com);
 */
public class JavaColumn {
    private String type = null;
    private String name = null;
    private int len = 128;
    private Method setMethod = null;
    private Method getMethod = null;
    private boolean key = false;
    private boolean hashValue = false;
    private static Logger logger = LoggerFactory.getLogger(JavaColumn.class);
    
    public JavaColumn(Method m){
        this.name = m.getName().substring(3);
        this.type = m.getParameterTypes()[0].getSimpleName().toLowerCase();
        this.setMethod = m;
        Annotation a =  m.getAnnotation(JDBCStoreKey.class);
        if(a!=null){
            this.key = true;
        }
        a =  m.getAnnotation(JDBCStoreHashValue.class);
        if(a!=null){
            this.hashValue = true;
        }
        String getName = "get"+this.setMethod.getName().substring(3);
        try {
            this.getMethod = m.getDeclaringClass().getMethod(getName,(Class<?>[]) null);
        } catch (NoSuchMethodException | SecurityException e) {
            logger.error("Failed to find get method for "+getName,e);
        }
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getLen() {
        return len;
    }

    public boolean isKey(){
        return this.key;
    }

    public boolean isHashValue() {
        return hashValue;
    }

    public void copyValue(Object fromPOJO,Object toPOJO){
        try {
           Object value = this.getMethod.invoke(fromPOJO, (Object[])null);
           if(value!=null)
               this.setMethod.invoke(toPOJO, new Object[]{value});
       } catch (IllegalAccessException | IllegalArgumentException
               | InvocationTargetException e) {
           logger.error("Failed to copy value",e);
       }
        
    }

    public void setFromRS(Object pojo,ResultSet rs){
        if(this.type.toLowerCase().equals("string")){
            try {
                this.setMethod.invoke(pojo, new Object[]{rs.getString(this.name)});
            } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | SQLException e) {
                logger.error("Failed to set value to "+name,e);
            }    
        }else
       if(this.type.toLowerCase().equals("int") || this.type.toLowerCase().equals("integer")){
           try {
               this.setMethod.invoke(pojo, new Object[]{rs.getInt(this.name)});
           } catch (IllegalAccessException | IllegalArgumentException
                   | InvocationTargetException | SQLException e) {
               logger.error("Failed to set value to "+name,e);
           }    
       }else
       if(this.type.toLowerCase().equals("boolean")){
           try {
               int value = rs.getInt(this.name);
               if(value==0){
                   this.setMethod.invoke(pojo, new Object[]{new Boolean(false)});
               }else{
                   this.setMethod.invoke(pojo, new Object[]{new Boolean(true)});
               }
           } catch (IllegalAccessException | IllegalArgumentException
                   | InvocationTargetException | SQLException e) {
               logger.error("Failed to set value to "+name,e);
           }
       }
   }

   public String getForSQL(Object pojo){
       Object value = null;
       try {
           value = this.getMethod.invoke(pojo,(Object[]) null);
       } catch (IllegalAccessException | IllegalArgumentException
               | InvocationTargetException e) {
           logger.error("Failed to fetch value for "+getMethod,e);
       }
       if(value==null)
           return null;
       if(this.type.equals("string")){
               return "'"+value.toString()+"'";
       }else{
           return value.toString();
       }
   }

   public void setValue(Object pojo,Object value){
       try {
        this.setMethod.invoke(pojo, new Object[]{value});
    } catch (IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
        logger.error("Failed to set the value",e);
    }
   }
}