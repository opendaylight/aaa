package org.opendaylight.aaa.idm.persistence;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLiteStore implements IOStore{
    private static Logger logger = LoggerFactory.getLogger(SQLiteStore.class);
    private Map<String,String> tableExist = new ConcurrentHashMap<>();
    
    public Connection dbConnect(OStore o) throws StoreException,SQLException {
        Connection conn;
        try {
           conn = OStoreConnectionPool.getDBConnect();
        }catch (StoreException se) {
           throw se;
        }catch(SQLException sle){
            throw sle;
        }
        try {
           if(!tableExist.containsKey(o.getProxyClass().getSimpleName())){
        	   tableExist.put(o.getProxyClass().getSimpleName(), o.getProxyClass().getSimpleName());
	           DatabaseMetaData dbm = conn.getMetaData();
	           ResultSet rs = dbm.getTables(null, null, o.getProxyClass().getSimpleName(), null);
	           if (rs.next()) {
	              debug(o.getProxyClass().getSimpleName()+" Table already exists");
	           }
	           else
	           {
	               try{createTable(conn,o);}catch(Exception err){err.printStackTrace();}
	           }
           }
        }
        catch (SQLException sqe) {
           throw new StoreException("Cannot connect to database server "+ sqe);
        }
        return conn;
    }
        
    public void createTable(Object connection,OStore o) throws Exception {
    	Connection conn = (Connection)connection;
        StringBuffer sql = new StringBuffer("CREATE TABLE ");
        sql.append(o.getProxyClass().getSimpleName()).append(" ");
        sql.append("("+OStore.getKeyMethodName(o)+" INTEGER PRIMARY KEY AUTOINCREMENT");
        String keys[] = OStore.collectKeys(o.getProxyClass());
        
        for(String key:keys){
        	Method m = OStore.getMethod(o,key);
            if(m.getReturnType().equals(String.class)){
                sql.append(",\n").append(key).append("  VARCHAR(128)      NOT NULL");
            }else
            if(m.getReturnType().equals(Integer.class) || m.getReturnType().equals(int.class)){
                sql.append(",\n").append(key).append("  INTEGER          NOT NULL");                
            }else
            if(m.getReturnType().equals(Boolean.class) || m.getReturnType().equals(boolean.class)){
                sql.append(",\n").append(key).append("  INTEGER          NOT NULL");                
            }
        }
        sql.append(")");
        
        logger.info(o.getProxyClass()+" Table does not exist, creating table");
        Statement stmt = null;
        stmt = conn.createStatement();        
        stmt.executeUpdate(sql.toString());
        stmt.close(); 
    }

    public PreparedStatement createInsertStatement(OStore o,Object connection) throws Exception{
        StringBuffer sql = new StringBuffer("insert into "+o.getProxyClass().getSimpleName()+" (");
        StringBuffer values = new StringBuffer(" values(");
        Connection conn = (Connection)connection;
        String keys[] = OStore.collectKeys(o.getProxyClass());
        boolean first = true;
        for(String key:keys){            
            if(!first){
                sql.append(",");
                values.append(",");
            }
            first = false;            
            sql.append(key);
            values.append("?");
        }
        sql.append(")").append(values).append(")");
        PreparedStatement statement = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
        int index = 1;
        for(String key:keys){
            Object value = o.getData().get(key);
            if(value==null){
            	Method m = OStore.getMethod(o,key);
                if(m.getReturnType().equals("String"))
                    value = "";
                else
                if(m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class))
                    value = new Boolean(false);
                else
                if(m.getReturnType().equals(int.class) || m.getReturnType().equals(Integer.class))
                    value = 0;
            }

            if(value instanceof String){
                statement.setString(index,(String)value);                    
            }else
            if(value instanceof Boolean){
                statement.setInt(index,(Boolean)value?1:0);                    
            }else
            if(value instanceof Integer){
                statement.setInt(index,(Integer)value);                    
            }            
            index++;
        }
        return statement;
    }
     
    public PreparedStatement createUpdateStatement(OStore o,Object connection) throws Exception{
        StringBuffer sql = new StringBuffer("Update "+o.getProxyClass().getSimpleName()+" SET ");        
        String keys[] = OStore.collectKeys(o.getProxyClass());
        Connection conn = (Connection)connection;
        boolean first = true;
        for(String key:keys){            
            if(!first){
                sql.append(",");
            }
            first = false;            
            sql.append(key);
            sql.append(" = ?");
        }
        sql.append(" WHERE "+OStore.getKeyMethodName(o)+"="+o.getData().get(OStore.getKeyMethodName(o)));        
        PreparedStatement statement = conn.prepareStatement(sql.toString());
        int index = 1;
        for(String key:keys){
            Object value = o.getData().get(key);
            if(value==null){
            	Method m = OStore.getMethod(o,key);
                if(m.getReturnType().equals("String"))
                    value = "";
                else
                if(m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class))
                    value = new Boolean(false);
                else
                if(m.getReturnType().equals(int.class) || m.getReturnType().equals(Integer.class))
                    value = 0;
            }

            if(value instanceof String){
                statement.setString(index,(String)value);                    
            }else
            if(value instanceof Boolean){
                statement.setInt(index,(Boolean)value?1:0);                    
            }else
            if(value instanceof Integer){
                statement.setInt(index,(Integer)value);                    
            }            
            index++;
        }
        return statement;
    }
        
    public void write(OStore o) throws Exception{
        int key=0;
        Connection conn = null;
        conn = dbConnect(o);        	
        PreparedStatement statement = null;
        boolean isUpdate = false;
        if(o.getData().get(OStore.getKeyMethodName(o))==null){
            statement = createInsertStatement(o,conn);
        }else{
            statement = createUpdateStatement(o,conn);
            isUpdate = true;
        }
        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0){
        	if(isUpdate)
        		throw new StoreException("Updating "+o.getProxyClass().getSimpleName()+"failed, no rows affected.");                   
        	else
        		throw new StoreException("Creating "+o.getProxyClass().getSimpleName()+"failed, no rows affected.");
        }
        if(!isUpdate){
        	ResultSet generatedKeys = statement.getGeneratedKeys();
        	if (generatedKeys.next())
        		key = generatedKeys.getInt(1);
        	else
        		throw new StoreException("Creating "+o.getProxyClass().getSimpleName()+" failed, no generated key obtained.");
        	o.getData().put(OStore.getKeyMethodName(o), key);               
        }
        OStoreConnectionPool.closeConnection(conn, logger);
    }
    
    public IStorable get(OStore o) throws Exception {
        List<IStorable> list = find(o);
        if(list.isEmpty())
            return null;
        return list.get(0);
    }
    
    public List<IStorable> find(OStore o) throws Exception{
        Connection conn = dbConnect(o);
        try{
            Statement stmt=null;
            String keys[] = OStore.collectKeys(o.getProxyClass());
            StringBuffer sql = new StringBuffer("SELECT * FROM ");
            sql.append(o.getProxyClass().getSimpleName());
            if(!o.getData().isEmpty()){
                sql.append(" WHERE ");
                boolean first = true;
                if(o.getData().get(OStore.getKeyMethodName(o))!=null){
                    sql.append(OStore.getKeyMethodName(o)).append("=");
                    sql.append(o.getData().get(OStore.getKeyMethodName(o)));
                    first = false;
                }
                sql.append(" ");
                for(String key:keys){
                    Object value = o.getData().get(key);
                    if(value!=null){
                    	if(!first) sql.append("and ");
                    	first = false;
                        sql.append(key).append("=");
                        Class returnType = OStore.getMethod(o, key).getReturnType();
                        if(value instanceof String){
                            sql.append("'").append(value).append("'");
                        }else
                        if(returnType.equals(boolean.class) || returnType.equals(Boolean.class)){
                        	if((boolean)value){
                        		sql.append("1");
                        	}else{
                        		sql.append("0");
                        	}
                        }else
                            sql.append(value);
                        sql.append(" ");
                    }
                }
            }

            stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql.toString());
            List<IStorable> resultList = convert(rs, keys,o);
            return resultList;
        }finally{
            OStoreConnectionPool.closeConnection(conn, logger);            
        }
    }
    
    public List<IStorable> convert(Object resultSet,String keys[],OStore o) throws SQLException,NoSuchMethodException {
    	ResultSet rs = (ResultSet)resultSet;
        List<IStorable> result = new LinkedList<>();
        while(rs.next()){
            IStorable st = OStore.newStorable(o.getProxyClass());
            OStore e = OStore.getStoreObject(st);
            e.getData().put(OStore.getKeyMethodName(o), rs.getInt(OStore.getKeyMethodName(o)));
            for(String key:keys){
                Method m = OStore.getMethod(o,key);
                if(m.getReturnType().equals(String.class)){
                    e.getData().put(key, rs.getString(key));
                }else
                if(m.getReturnType().equals(int.class) || m.getReturnType().equals(Integer.class)){
                    e.getData().put(key, rs.getInt(key));                    
                }else
                if(m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class)){
                    int i = rs.getInt(key);
                    if(i==0)
                        e.getData().put(key, new Boolean(false));
                    else
                        e.getData().put(key, new Boolean(true));
                }                
            }
            result.add(st);
        }
        return result;
    }    
    
    public List<IStorable> delete(OStore o) throws Exception{
        Connection conn = dbConnect(o);
        try{
            Statement stmt=null;
            String keys[] = OStore.collectKeys(o.getProxyClass());
            StringBuffer sql = new StringBuffer(" FROM ");
            sql.append(o.getProxyClass().getSimpleName());

            if(!o.getData().isEmpty()){
                sql.append(" WHERE ");
                boolean first = true;
                if(o.getData().get(OStore.getKeyMethodName(o))!=null){
                    sql.append(OStore.getKeyMethodName(o)).append("=");
                    sql.append(o.getData().get(OStore.getKeyMethodName(o)));
                    first = false;
                }
                sql.append(" ");
                for(String key:keys){
                    Object value = o.getData().get(key);
                    if(value!=null){
                    	if(!first) sql.append("and ");
                    	first = false;
                        sql.append(key).append("=");
                        Class returnType = OStore.getMethod(o, key).getReturnType();
                        if(value instanceof String){
                            sql.append("'").append(value).append("'");
                        }else
                        if(returnType.equals(boolean.class) || returnType.equals(Boolean.class)){
                        	if((boolean)value){
                        		sql.append("1");
                        	}else{
                        		sql.append("0");
                        	}
                        }else
                            sql.append(value);
                        sql.append(" ");
                    }
                }
            }
            stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery("SELECT * "+sql.toString());
            List<IStorable> resultList = convert(rs, keys,o);
            stmt.close();
            stmt = conn.createStatement();
            stmt.execute("DELETE "+sql.toString());
            stmt.close();            
            return resultList;
        }finally{
            OStoreConnectionPool.closeConnection(conn, logger);            
        }
    }
        
    private static final void debug(String msg) {
        if (logger.isDebugEnabled())
            logger.debug(msg);
    }
    
}
