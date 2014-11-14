package org.opendaylight.aaa.idm.persistence;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class CassandraStore implements IOStore{
    private static Session session = null;
    private Map<String,String> tableExist = new ConcurrentHashMap<>();
    
    private void validateTableExist(OStore o){
    	if(tableExist.containsKey(getValideTableName(o)))
    		return;
    	
        try{
        	com.datastax.driver.core.ResultSet rs =  session.execute("select count(*) from "+getValideTableName(o));
            tableExist.put(getValideTableName(o), getValideTableName(o));        	
        }catch(InvalidQueryException err){
        	try{
        		createTable(session, o);
                tableExist.put(getValideTableName(o), getValideTableName(o));        		
        	}catch(Exception er){
        		OStore.log(er);        		
        	}
        }
    }
    
    public Object dbConnect(Class<? extends IStorable> proxyClass) throws Exception {
    	if(session==null){
    		synchronized(this){    		 
        		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("/root/cassandra.ini")));        		
        		String line = in.readLine().trim();
        		OStore.log("Trying to work with "+line);
        		in.close();    			
    	    	Cluster cluster = Cluster.builder().addContactPoint(line).build();
    	    	try{
    	    		session = cluster.connect("aaa");
    	    		return session;
    	    	}catch(InvalidQueryException err){
    	    		session = cluster.connect();
    	    		session.execute("CREATE KEYSPACE aaa WITH replication " + 
    	    			      "= {'class':'SimpleStrategy', 'replication_factor':3};");
    	    		session = cluster.connect("aaa");
    	    		return session;
    	    	}    			
    		}
    	}
    	return session;
    }
    
    public static String getValideTableName(OStore o){
    	String tableName = o.getProxyClass().getSimpleName();
    	if(tableName.equals("Grant")){
    		tableName = "G"+tableName;
    	}
    	return tableName;
    }
    
    public void createTable(Object connection,OStore o) throws Exception {
    	    	
    	Session session = (Session)connection;
        StringBuffer sql = new StringBuffer("CREATE TABLE ");
        sql.append(getValideTableName(o)).append(" ");
        sql.append("("+OStore.getKeyMethodName(o)+" int PRIMARY KEY");
        String keys[] = OStore.collectKeys(o.getProxyClass());
        for(String key:keys){
        	Method m = OStore.getMethod(o,key);
            if(m.getReturnType().equals(String.class)){
                sql.append(",\n").append(key).append("  text");
            }else
            if(m.getReturnType().equals(Integer.class) || m.getReturnType().equals(int.class)){
                sql.append(",\n").append(key).append("  int");                
            }else
            if(m.getReturnType().equals(Boolean.class) || m.getReturnType().equals(boolean.class)){            	
                sql.append(",\n").append(key).append("  int");                
            }
        }
        sql.append(")");
        
        com.datastax.driver.core.ResultSet rs = session.execute(sql.toString());
    }

    public String createInsertStatement(OStore o) throws Exception{
        StringBuffer sql = new StringBuffer("insert into "+getValideTableName(o)+" (");
        StringBuffer values = new StringBuffer(" values(");
        String keys[] = OStore.collectKeys(o.getProxyClass());
        boolean first = true;
        int hashKey = 0;
        for(String key:keys){
        	if(!first){
	            sql.append(",");
	            values.append(",");
        	}
        	first = false;
            sql.append(key);
            Object value = o.getData().get(key);
            if(value==null){
            	Method m = OStore.getMethod(o,key);
                if(m.getReturnType().equals(String.class))
                    value = "";
                else
                if(m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class))
                    value = new Boolean(false);
                else
                if(m.getReturnType().equals(int.class) || m.getReturnType().equals(Integer.class))
                    value = 0;
            }
            if(value instanceof String){
            	if(hashKey==0){
            		hashKey = value.hashCode();
            	}else{
            		hashKey = hashKey | value.hashCode();
            	}
                values.append("'").append(value).append("'");                    
            }else
            if(value instanceof Boolean){
            	values.append((Boolean)value?1:0);                    
            }else
            if(value instanceof Integer){
            	values.append(value);                    
            }                        
        }
        
        sql.append(",").append(OStore.getKeyMethodName(o));
        values.append(",").append(hashKey);
        o.getData().put(OStore.getKeyMethodName(o), hashKey);
        
        sql.append(")").append(values).append(")");        
        return sql.toString();
    }
     
    public String createUpdateStatement(OStore o) throws Exception{
        StringBuffer sql = new StringBuffer("Update "+getValideTableName(o)+" SET ");        
        String keys[] = OStore.collectKeys(o.getProxyClass());
        boolean first = true;
        for(String key:keys){            
            if(!first){
                sql.append(",");
            }
            first = false;            
            sql.append(key);
            sql.append(" = ");
            Object value = o.getData().get(key);
            if(value==null){
            	Method m = OStore.getMethod(o,key);
                if(m.getReturnType().equals(String.class))
                    value = "";
                else
                if(m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class))
                    value = new Boolean(false);
                else
                if(m.getReturnType().equals(int.class) || m.getReturnType().equals(Integer.class))
                    value = 0;
            }

            if(value instanceof String){
                sql.append("'").append(value).append("'");                    
            }else
            if(value instanceof Boolean){
            	sql.append((Boolean)value?1:0);                    
            }else
            if(value instanceof Integer){
            	sql.append(value);                    
            }                        
            
        }
        sql.append(" WHERE "+OStore.getKeyMethodName(o)+"="+o.getData().get(OStore.getKeyMethodName(o)));
        return sql.toString();
    }
        
    public void write(OStore o) throws Exception{
        int key=0;
        Session session = (Session)dbConnect(o.getProxyClass());
        validateTableExist(o);
        boolean isUpdate = false;
        String sql = null;
        if(o.getData().get(OStore.getKeyMethodName(o))==null){
            sql = createInsertStatement(o);
        }else{
            sql= createUpdateStatement(o);
            isUpdate = true;
        }        
        com.datastax.driver.core.ResultSet rs = session.execute(sql);
        int affectedRows = 1;
        if (affectedRows == 0){
        	if(isUpdate)
        		throw new StoreException("Updating "+getValideTableName(o)+"failed, no rows affected.");                   
        	else
        		throw new StoreException("Creating "+getValideTableName(o)+"failed, no rows affected.");
        }
    }
    
    public IStorable get(OStore o) throws Exception {
        List<IStorable> list = find(o);
        if(list.isEmpty())
            return null;
        return list.get(0);
    }
    
    public List<IStorable> find(OStore o) throws Exception{
        Session session = (Session)dbConnect(o.getProxyClass());

        try{
            String keys[] = OStore.collectKeys(o.getProxyClass());
            StringBuffer sql = new StringBuffer("SELECT * FROM ");
            sql.append(getValideTableName(o));
            if(o.getData().get(OStore.getKeyMethodName(o))!=null){
            	sql.append(" WHERE ").append(OStore.getKeyMethodName(o)).append("=").append(o.getData().get(OStore.getKeyMethodName(o)));
            }
            com.datastax.driver.core.ResultSet rs = session.execute(sql.toString());
            List<IStorable> resultList = convert(rs, keys,o);
            return resultList;
        }finally{            
        }
    }
    
    public List<IStorable> convert(Object resultSet,String keys[],OStore o) throws Exception {
    	com.datastax.driver.core.ResultSet rs = (com.datastax.driver.core.ResultSet)resultSet;
        List<IStorable> result = new LinkedList<>();
        List<Row> rows = rs.all();
        
        for(Row row:rows){
            IStorable st = OStore.newStorable(o.getProxyClass());
            OStore e = OStore.getStoreObject(st);
            e.getData().put(OStore.getKeyMethodName(o), row.getInt(OStore.getKeyMethodName(o)));
            boolean fitCriteria = true;
            for(String key:keys){
                Method m = OStore.getMethod(o,key);
                if(m.getReturnType().equals(String.class)){
                    e.getData().put(key, row.getString(key));
                }else
                if(m.getReturnType().equals(int.class) || m.getReturnType().equals(Integer.class)){
                    e.getData().put(key, row.getInt(key));                    
                }else
                if(m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class)){
                    int i = row.getInt(key);
                    if(i==0)
                        e.getData().put(key, new Boolean(false));
                    else
                        e.getData().put(key, new Boolean(true));
                }
            	Object origValue = o.getData().get(key);                
                if(origValue!=null){
                	Object recValue = e.getData().get(key);
                	if(recValue==null){
                		fitCriteria = false;
                		break;
                	}else
                	if(!recValue.equals(origValue)){
                		fitCriteria = false;
                		break;                		
                	}
                }
            }
            if(fitCriteria)
            	result.add(st);
        }
        return result;
    }    
    
    public List<IStorable> delete(OStore o) throws Exception{
        Session session = (Session)dbConnect(o.getProxyClass());
        
        try{
            String keys[] = OStore.collectKeys(o.getProxyClass());
            StringBuffer sql = new StringBuffer(" FROM ");
            sql.append(getValideTableName(o));

            if(o.getData().get(OStore.getKeyMethodName(o))!=null){
            	sql.append(" WHERE ").append(OStore.getKeyMethodName(o)).append("=").append(o.getData().get(OStore.getKeyMethodName(o)));
            }
            com.datastax.driver.core.ResultSet rs = session.execute("Select * "+sql.toString());
            List<IStorable> resultList = convert(rs, keys,o);
            session.execute("Delete "+sql.toString());
            return resultList;
        }finally{            
        }
    }    
}
