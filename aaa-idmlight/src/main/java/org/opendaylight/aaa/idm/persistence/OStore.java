package org.opendaylight.aaa.idm.persistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.aaa.idm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OStore implements InvocationHandler{
    private static Logger logger = LoggerFactory.getLogger(OStore.class);    
    protected Map<String,Object> data = new HashMap<>();
    private Class<? extends IStorable> proxyClass = null;
    private IStorable myProxy = null;
    
    private static IOStore store = new SQLiteStore();
    //private static IOStore store = new CassandraStore();
    private static Map<Class,CacheEntry> cache = new HashMap<>();
    private static class CacheEntry {
    	String keyMethodName = null;
    	String keys[] = null;
    	Map<String,Method> keyToMethod = new HashMap<>(); 
    }
    
    public OStore(Class<? extends IStorable> cls){
        this.proxyClass = cls;
        collectKeys(cls);
    }
        
    @Override
    public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
        String mname = arg1.getName();
        if(mname.equals("toString")){
        	return this.toString();
        }else
        if(mname.equals("update")){
           store.write(this);
           return arg0;
        }else
        if(mname.equals("deleteAll")){
            return store.delete(this);        	
        }else
        if(mname.equals("delete")){
           List<IStorable> lst = store.delete(this);
           if(lst.isEmpty())
        	   return null;
           else
        	  return lst.get(0);
        }else
        if(mname.equals("get")){
            return store.get(this);
        }else
        if(mname.equals("find")){
            return store.find(this);
        }else        
        if(mname.equals("write")){
            store.write(this);
            return arg0;
        }else
        if(mname.startsWith("get")){
            return data.get(arg1.getName().substring(3));
        }else
        if(mname.startsWith("set")){
            data.put(mname.substring(3), arg2[0]);
        }
        return null;
    }
    
    public String toString(){
    	return this.proxyClass.getSimpleName();
    }
    
    public Class<? extends IStorable> getProxyClass(){
    	return this.proxyClass;
    }
    
    public static IStorable newStorable(Class<? extends IStorable> cls){
        OStore so = new OStore(cls);
        so.myProxy = (IStorable)Proxy.newProxyInstance(OStore.class.getClassLoader(), new Class[]{cls}, new OStore(cls)); 
        return so.myProxy;
    }

    protected static OStore getStoreObject(Object proxy){
        return (OStore)Proxy.getInvocationHandler(proxy);
    }
                        
    protected static Method getMethod(Class cls,String key){
    	CacheEntry e = cache.get(cls);
    	return e.keyToMethod.get(key);
    }

    protected static String getKeyMethodName(Class cls){
    	CacheEntry e = cache.get(cls);
    	return e.keyMethodName;
    }

    protected static String[] collectKeys(Class<? extends IStorable> cls){
    	CacheEntry e = cache.get(cls);
    	if(e!=null)
    		return e.keys;
    	e = new CacheEntry();
    	cache.put(cls, e);
    	
        Method methods[] = cls.getDeclaredMethods();
        List<String> result = new ArrayList<>();
        for(Method m:methods){
        	Annotation ann = m.getAnnotation(KeyMethod.class);
        	if(ann!=null){
        		e.keyMethodName = m.getName().substring(3);
        		continue;
        	}
        	
            if(m.getName().startsWith("set")){
            	String key = m.getName().substring(3); 
                result.add(key);
                Method getM = null;
                try{
                	getM = cls.getMethod("get"+key, null);
                }catch(Exception err){err.printStackTrace();}
                e.keyToMethod.put(key, getM);
            }
        }
        e.keys = result.toArray(new String[result.size()]);
        return e.keys;
    }

    private static final void debug(String msg) {
        if (logger.isDebugEnabled())
            logger.debug(msg);
    }
    
    public static void main(String args[]){
    	for(int i=0;i<100;i++){
	    	User user = (User)OStore.newStorable(User.class);
	        user.setName("Test-"+i);
	        user.setEmail("Test-"+i+"@Tests.com");
	        user.setEnabled(true);
	        user.setDescription("Testing 1..2..3");
	        user.setPassword("Hello");
	    	user = (User)user.write();
    	}
    	
    	User user = (User)OStore.newStorable(User.class);        
    	List<IStorable> find = user.find();

    	user.setName("Hello World");
        user.setEmail("Test-"+1+"@Tests.com");
        user.setEnabled(true);
        user.setDescription("Testing 1..2..3");
        user.setPassword("Hello");
        user.setUserid(-25166850);
        
    	user = (User)user.update();
    	    	
    	User del = (User)user.delete();
    	System.out.println(user.getUserid()+":"+user.getName());
    }
}
