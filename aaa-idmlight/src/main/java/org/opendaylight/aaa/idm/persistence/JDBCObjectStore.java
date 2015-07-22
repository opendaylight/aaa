/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.opendaylight.aaa.idm.persistence;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opendaylight.aaa.idm.IdmLightApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @Author - Sharon Aicler (saichler@cisco.com);
 */
public class JDBCObjectStore {
    private static Logger logger = LoggerFactory.getLogger(JDBCObjectStore.class);
    protected Connection  dbConnection = null;
    protected IJDBCStoreVisitor store = new GenericSQLVisitor();
    private static Map<String,String> tblName2ClassName = new HashMap<String,String>();
    private static Map<Class<?>,String> class2TableName = new HashMap<Class<?>,String>();
    private static Map<Class<?>,List<JavaColumn>> class2Columns = new HashMap<Class<?>,List<JavaColumn>>();
    private static Map<Class<?>,JavaColumn> class2KeyColumn = new HashMap<Class<?>,JavaColumn>();

    protected Connection getDBConnect() throws StoreException {
        dbConnection = IdmLightApplication.getConnection(dbConnection);
        return dbConnection;
    }

    protected void dbClose() {
        if (dbConnection != null)
        {
           try {
              dbConnection.close ();
            }
            catch (Exception e) {
              logger.error("Cannot close Database Connection " + e);
            }
         }
    }

    public static final void clearCache(){
        tblName2ClassName.clear();
        class2TableName.clear();
        class2Columns.clear();
    }

    public Connection dbConnect(Object pojo) throws StoreException {
        Connection conn;
        String pojoTable = getPOJOTableName(pojo);
        try {
           conn = getDBConnect();
        }
        catch (StoreException se) {
           throw se;
        }
        try {
            if(!tblName2ClassName.containsKey(pojoTable)){
                tblName2ClassName.put(pojoTable, pojo.getClass().getName());
            }else{
                return conn;
            }
            DatabaseMetaData dbm = conn.getMetaData();
            String[] tableTypes = {"TABLE"};
            ResultSet rs = dbm.getTables(null, null,pojoTable.toUpperCase(), tableTypes);
            if (rs.next()) {
                debug(pojoTable+" Table already exists");
            } else {
                logger.info(pojoTable+" table does not exist, creating table");
                StringBuilder sql = new StringBuilder("CREATE TABLE "+pojoTable+" (");
                String key = null;
                List<JavaColumn> cols = getColumnList(pojo);
                Statement stmt = null;
                stmt = conn.createStatement();
                boolean isFirst = true;
                for(JavaColumn col:cols){
                    String type = store.getStoreDataTypeName(col.getType());
                    if(!isFirst)
                        sql.append(",");
                    if(col.isKey()){
                        key = "CONSTRAINT "+pojoTable+"_KEY PRIMARY KEY ("+col.getName()+")";
                        class2KeyColumn.put(pojo.getClass(), col);
                    }
                    if(type.endsWith("(")){
                        sql.append(col.getName());
                        sql.append(" ");
                        sql.append(type);
                        sql.append(col.getLen());
                        sql.append(") ");
                        sql.append(store.getStoreNotNullStatement());
                    }else{
                        sql.append(col.getName());
                        sql.append(" ");
                        sql.append(type);
                        sql.append(" ");
                        sql.append(store.getStoreNotNullStatement());
                    }
                    isFirst = false;
                }
                if(key!=null){
                    sql.append(",").append(key);
                }
                sql.append(")");
                stmt.executeUpdate(sql.toString());
                stmt.close();
            }
        }
        catch (SQLException sqe) {
           throw new StoreException("Cannot connect to database server "+ sqe);
        }
        return conn;
    }

    protected Object rsToObject(ResultSet rs) throws SQLException {
        String tableName = rs.getMetaData().getTableName(1);
        Object pojo;
        try {
            pojo = Class.forName(tblName2ClassName.get(tableName)).newInstance();
            List<JavaColumn> columns = getColumnList(pojo);
            for(JavaColumn col:columns){
                col.setFromRS(pojo, rs);
            }
            return pojo;            
        } catch (InstantiationException | IllegalAccessException
                | ClassNotFoundException e) {
            logger.error("Failed to instanciate "+tableName+" object",e);
        }
        return null;
    }

    public Object createPOJO(Object pojo) throws StoreException {
        String pojoTable = getPOJOTableName(pojo);
        Connection conn = dbConnect(pojo);
        try {
            List<JavaColumn> cols = getColumnList(pojo);
            StringBuilder sql = new StringBuilder();
            StringBuilder values = new StringBuilder(" values(");
            sql.append("insert into ").append(pojoTable).append(" (");
            boolean isFirst = true;
            for(JavaColumn col:cols){
                if(!isFirst){
                    sql.append(",");
                    values.append(",");
                }
                sql.append(col.getName());
                String value = col.getForSQL(pojo);
                if(value!=null)
                    values.append(value);
                else
                if(col.isHashValue()){
                    Object v = class2KeyColumn.get(pojo.getClass()).getForSQL(pojo);
                    values.append(v.hashCode());
                    col.setValue(pojo, v.hashCode());
                }else
                if(col.getType().toLowerCase().equals("string")){
                    values.append("''");
                }else
                    values.append("0");
                isFirst = false;
                
            }
            sql.append(")");
            values.append(")");
            sql.append(values);
            Statement st = conn.createStatement();
            st.execute(sql.toString());            
            return pojo;
        }
        catch (SQLException s) {
           throw new StoreException("SQL Exception : " + s);
        }
        finally {
           dbClose();
         }
    }

    public Object updatePOJO(Object pojo) throws StoreException {
        Object existingPOJO = deletePOJO(pojo,true);
        if(existingPOJO==null)
            return null;
        List<JavaColumn> cols = getColumnList(pojo);
        for(JavaColumn col:cols){
            col.copyValue(pojo, existingPOJO);
        }
        this.createPOJO(existingPOJO);
        return existingPOJO;
    }

    public List<Object> getPOJOs(Object pojo, boolean useOnlyKey) throws StoreException {
        Connection conn = dbConnect(pojo);
        String pojoTable = getPOJOTableName(pojo);
        List<JavaColumn> cols = getColumnList(pojo);
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(pojoTable);
        boolean isFirst = true;
        for(JavaColumn col:cols){
            if(!useOnlyKey || col.isKey()){
                String val = col.getForSQL(pojo);
                if(val!=null){
                    if(!isFirst){
                        sql.append(" and ");
                    }else{
                        sql.append(" where ");
                    }
                    isFirst = false;
                    sql.append(col.getName()).append("=").append(val);
                }
            }
        }
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql.toString());
            List<Object> result = new ArrayList<Object>();
            while(rs.next()){
                result.add(rsToObject(rs));
            }
            return result;
        }catch (SQLException s) {
           throw new StoreException("SQL Exception : " + s);
        }
        finally {
            if(rs!=null)try{rs.close();}catch(Exception err){}
            if(st!=null)try{st.close();}catch(Exception err){}
            dbClose();
        }
    }    

    public Object getPOJO(Object pojo, boolean useOnlyKey) throws StoreException {
        Connection conn = dbConnect(pojo);
        String pojoTable = getPOJOTableName(pojo);
        List<JavaColumn> cols = getColumnList(pojo);
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(pojoTable);
        boolean isFirst = true;
        for(JavaColumn col:cols){
            if(!useOnlyKey || col.isKey()){
                String val = col.getForSQL(pojo);
                if(val!=null){
                    if(!isFirst){
                        sql.append(" and ");
                    }else{
                        sql.append(" where ");
                    }
                    isFirst = false;
                    sql.append(col.getName()).append("=").append(val);
                }
            }
        }
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql.toString());
            if(rs.next()){
                return rsToObject(rs);
            }
        }catch (SQLException s) {
           throw new StoreException("SQL Exception : " + s);
        }
        finally {
            if(rs!=null)try{rs.close();}catch(Exception err){}
            if(st!=null)try{st.close();}catch(Exception err){}
            dbClose();
        }
        return null;
    }

    public Object deletePOJO(Object pojo,boolean useOnlyKey) throws StoreException {
        Connection conn = dbConnect(pojo);
        String pojoTable = getPOJOTableName(pojo);
        List<JavaColumn> cols = getColumnList(pojo);
        StringBuilder sql = new StringBuilder("select * from ");
        StringBuilder deleteSql = new StringBuilder("delete from ");
        sql.append(pojoTable);
        deleteSql.append(pojoTable);
        boolean isFirst = true;
        for(JavaColumn col:cols){
            if(!useOnlyKey || col.isKey()){
                String val = col.getForSQL(pojo);
                if(val!=null){
                    if(!isFirst){
                        sql.append(" and ");
                        deleteSql.append(" and ");
                    }else{
                        sql.append(" where ");
                        deleteSql.append(" where ");
                    }
                    isFirst = false;
                    sql.append(col.getName()).append("=").append(val);
                    deleteSql.append(col.getName()).append("=").append(val);
                }
            }
        }
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql.toString());
            if(rs.next()){
                Object result = rsToObject(rs);
                if(result==null){
                    return null;
                }
                conn.createStatement().execute(deleteSql.toString());
                return result;
            }
        }catch (SQLException s) {
           throw new StoreException("SQL Exception : " + s);
        }
        finally {
            if(rs!=null)try{rs.close();}catch(Exception err){}
            if(st!=null)try{st.close();}catch(Exception err){}
            dbClose();
        }
        return null;
    }

    public List<Object> getAllObjects(Object pojo) throws StoreException {
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(getPOJOTableName(pojo));
        Connection conn = dbConnect(pojo);
        Statement st = null;
        ResultSet rs = null;
        List<Object> result = new LinkedList<Object>();
        try{
            st = conn.createStatement();
            rs = st.executeQuery(sql.toString());
            while(rs.next()){
                result.add(rsToObject(rs));
            }
        }catch(Exception err){
            logger.error("Failed to get all records",err);
        }finally{
            if(rs!=null)try{rs.close();}catch(Exception err){}
            if(st!=null)try{st.close();}catch(Exception err){}
        }
        return result;
    }

    public void closeConnection(){
        if(this.dbConnection!=null){
            try {
                this.dbConnection.close();
            } catch (SQLException e) {
                logger.error("Failed to close the connection",e);
            }
        }
    }
    
    private static final void debug(String msg) {
         if (logger.isDebugEnabled()) {
             logger.debug(msg);
         }
    }

    private static final String getPOJOTableName(Object pojo){
        String result = class2TableName.get(pojo.getClass());
        if(result!=null){
            return result;
        }
        result = (pojo.getClass().getSimpleName()+"_TBL").toUpperCase();
        class2TableName.put(pojo.getClass(),result);
        return result;
    }

    private static final List<JavaColumn> getColumnList(Object pojo){
         List<JavaColumn> cols = class2Columns.get(pojo.getClass());
         if(cols!=null){
             return cols;
         }
         cols = new ArrayList<JavaColumn>();
         Method methods[] = pojo.getClass().getMethods();
         for(Method m:methods){
             if(m.getName().startsWith("set")){
                 JavaColumn col = new JavaColumn(m);
                 cols.add(col);
             }
         }
         class2Columns.put(pojo.getClass(), cols);
         return cols;
    }

}
