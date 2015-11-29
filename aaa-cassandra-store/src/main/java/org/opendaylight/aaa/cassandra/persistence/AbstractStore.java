/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cassandra.persistence;

/**
 *
 * @author saichler@gmail.com
 *
 */

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStore<T,E> {
    private static Logger logger = LoggerFactory.getLogger(AbstractStore.class);
    private final CassandraStore store;
    private final Class<?> elementType;
    private final Class<?> collectionType;
    private final Method collectionSetMethod;
    private List<Field> fields = new ArrayList<>();
    private Field keyField = null;

    protected AbstractStore(CassandraStore store,Class<?> elementType, Class<?> collectionType,String collectionMethodName,String keyMethodName) throws NoSuchMethodException{
        this.store = store;
        this.elementType = elementType;
        this.collectionType = collectionType;
        this.collectionSetMethod = this.collectionType.getMethod(collectionMethodName,new Class[]{List.class});
        Method methods[] = elementType.getMethods();
        for(Method m:methods){
            if(m.getName().startsWith("set")){
                Field f = new Field();
                f.javaSetMethod = m;
                f.name = m.getName().substring(3);
                f.javaType = m.getParameterTypes()[0].getSimpleName();
                if(f.javaType.equals("String")){
                    f.type = "text";
                    f.javaGetMethod = elementType.getMethod("get"+m.getName().substring(3),null);
                }else
                if(f.javaType.equals("Boolean")){
                    f.type = "boolean";
                    f.javaGetMethod = elementType.getMethod("is"+m.getName().substring(3),null);
                }
                if(m.getName().equals(keyMethodName)){
                    f.isKey = true;
                    this.keyField = f;
                }else{
                    f.isKey = false;
                }
                this.fields.add(f);
            }
        }
   }

   private static class Field {
      private String name;
      private String type;
      private Method javaSetMethod;
      private Method javaGetMethod;
      private String javaType;
      private boolean isKey = false;
   }

   public String getTableName(){
      return this.elementType.getSimpleName();
   }

   protected void dbClean() throws StoreException{
      try {
         Session session = store.getSession();
         String cql = "truncate table "+getTableName();
         session.execute(cql);
      } catch (IOException e) {
         throw new StoreException(e.getMessage());
      }
   }

   protected Session dbConnect() throws StoreException {
      try {
         Session conn = store.getSession();
         if (!store.doesTableExist(getTableName())) {
            String cql = "CREATE TABLE " + getTableName() + "(";
            for(Field f:this.fields){
               cql += f.name +" "+f.type + ",";
            }
            cql+= " Primary Key("+keyField.name+"))";
            conn.execute(cql);
         }
         return conn;
      }catch(IOException e){
         throw new StoreException("Failed to get session ");
      }
   }

   protected void dbClose() {
   }

   @Override
   protected void finalize () throws Throwable  {
      dbClose();
      super.finalize();
   }

   protected T rsToElement(Row row) {
      try {
         T element = (T) this.elementType.newInstance();
         for (Field f:fields){
            try {
               if(f.javaType.equals("String")) {
                  f.javaSetMethod.invoke(element, new Object[]{row.getString(f.name)});
               }else
               if(f.javaType.equals("Boolean")) {
                  f.javaSetMethod.invoke(element, new Object[]{row.getBool(f.name)});
               }
            } catch (InvocationTargetException e) {
               logger.error("Failed to set field value",e);
            }
         }
         return element;
      } catch (InstantiationException | IllegalAccessException e) {
         logger.error("Failed to instantiate element",e);
      }
      return null;
   }

   protected E getCollection() throws StoreException {
      try {
         E elements = (E) collectionType.newInstance();
         Session conn = dbConnect();
         String query = "SELECT * FROM " + getTableName();
         ResultSet rs = conn.execute(query);
         List<T> elementList = new ArrayList<T>();
         for (Row row : rs.all()) {
            T domain = rsToElement(row);
            elementList.add(domain);
         }
         collectionSetMethod.invoke(elements,new Object[]{elementList});
         return elements;
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
         logger.error("Failed to instantiate collection", e);
      }
      return null;
   }

   protected E getCollection(String id) throws StoreException {
      try {
         E elements = (E) collectionType.newInstance();
         Session conn = dbConnect();
         String query = "SELECT * FROM " + getTableName() + " where "+keyField.name +" = '"+id+"'";
         ResultSet rs = conn.execute(query);
         List<T> elementList = new ArrayList<T>();
         for (Row row : rs.all()) {
            T domain = rsToElement(row);
            elementList.add(domain);
         }
         collectionSetMethod.invoke(elements,new Object[]{elementList});
         return elements;
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
         logger.error("Failed to instantiate collection", e);
      }
      return null;
   }

   protected T getElement(String id) throws StoreException {
         Session conn = dbConnect();
         String query = "SELECT * FROM " + getTableName() + " where "+keyField.name+ " = '"+id+"'";
         ResultSet rs = conn.execute(query);
         for (Row row : rs.all()) {
            T element = rsToElement(row);
            return element;
         }
      return null;
   }

   private String createKey(T element){
      if(element instanceof Domain){
         return IDMStoreUtil.createDomainid(((Domain)element).getName());
      }else
      if(element instanceof Role){
         return IDMStoreUtil.createRoleid(((Role)element).getName(),((Role)element).getDomainid());
      }else
      if(element instanceof User){
         return IDMStoreUtil.createUserid(((User)element).getName(),((User)element).getDomainid());
      }else
      if(element instanceof Grant){
         return IDMStoreUtil.createGrantid(((Grant)element).getUserid(),((Grant)element).getDomainid(),((Grant)element).getRoleid());
      }
      return null;
   }

   protected T createElement(T element) throws StoreException {
      try {
         keyField.javaSetMethod.invoke(element,new Object[]{createKey(element)});
         Session conn = dbConnect();
         String cql = "insert into " + getTableName() + " (";
         String values = " values(";
         boolean first = true;
         for(Field f:this.fields){
            if(!first){
               cql+=",";
               values+=",";
            }
            cql +=f.name;
            if(f.javaType.equals("String")){
               values+="'"+f.javaGetMethod.invoke(element,null)+"'";
            }else
            if(f.javaType.equals("Boolean")){
               values+=f.javaGetMethod.invoke(element,null);
            }
            first = false;
         }
         cql+=")"+values+")";
         conn.execute(cql);
         return element;
      } catch (IllegalAccessException | InvocationTargetException e) {
         throw new StoreException(e.getMessage());
      }
   }

   protected T putElement(T element) throws StoreException {
      try {
         T savedElement = this.getElement((String)keyField.javaGetMethod.invoke(element,null));
         for(Field f:fields){
            if(f!=keyField){
               Object value = f.javaGetMethod.invoke(element,null);
               if(value!=null){
                  f.javaSetMethod.invoke(savedElement,new Object[]{value});
               }
            }
         }
         Session conn = dbConnect();
         String cql = "update " + getTableName() + " set ";
         boolean first = true;
         for(Field f:fields){
            if(f!=keyField){
               if(!first){
                  cql+=",";
               }
               Object value = f.javaGetMethod.invoke(savedElement,null);
               cql+=f.name+"=";
               if(f.javaType.equals("String")){
                  cql+="'"+value+"'";
               }
               first=false;
            }
         }
         cql+=" where "+keyField.name+"='"+keyField.javaGetMethod.invoke(savedElement,null)+"'";
         conn.execute(cql);
         return savedElement;
      } catch (IllegalAccessException | InvocationTargetException e) {
         throw new StoreException(e.getMessage());
      }
   }

   protected T deleteElement(String id) throws StoreException {
      T element = this.getElement(id);
      if(element==null){
         return null;
      }
      try {
         Session conn = store.getSession();
         String cql = "delete from " + getTableName() +
                 " where "+this.keyField.name+" ='" +keyField.javaGetMethod.invoke(element,null) +"'";
         conn.execute(cql);
         return element;
      } catch (IOException | InvocationTargetException | IllegalAccessException err){
         throw new StoreException(err.getMessage());
      }
   }

   private static final void debug(String msg) {
       if (logger.isDebugEnabled()) {
           logger.debug(msg);
       }
   }
}
