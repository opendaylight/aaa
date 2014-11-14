package org.opendaylight.aaa.idm.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.aaa.idm.model.Grant;
import org.opendaylight.aaa.idm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OStore implements InvocationHandler{
    private static Logger logger = LoggerFactory.getLogger(OStore.class);    
    private Map<String,Object> data = new HashMap<>();
    private Class<? extends IStorable> proxyClass = null;
    private IStorable myProxy = null;
    
    private static IOStore store = loadObjectStore();
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
    
    public Map<String,Object> getData(){
    	return this.data;
    }
    
    private static IOStore loadObjectStore(){
    	IOStore s = null;
    	try{
    		InputStream sin = OStore.class.getClassLoader().getResourceAsStream("store.ini");    		 
    		BufferedReader in = new BufferedReader(new InputStreamReader(sin));
    		String line = in.readLine().trim();
    		in.close();
    		int index = line.indexOf("class=");
    		String className = line.substring(index+6).trim();
    		File jars = new File("/tmp/jars");
    		if(!className.equals(SQLiteStore.class.getName()) && jars.exists() && jars.listFiles()!=null){
    			File files[] = jars.listFiles();
    			List<URL> urls = new ArrayList<URL>();
    			for(File f:files){
    				if(f.getName().endsWith(".jar")){
    					urls.add(f.toURL());
    				}
    			}
    			URLClassLoader cl = new URLClassLoader(urls.toArray(new URL[urls.size()]),OStore.class.getClassLoader());
    			return (IOStore)cl.loadClass(className).newInstance();
    		}else
    			return new SQLiteStore();
    	}catch(Exception err){
    		err.printStackTrace();
    	}
    	return null;
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

    public static OStore getStoreObject(Object proxy){
        return (OStore)Proxy.getInvocationHandler(proxy);
    }
                        
    public static Method getMethod(OStore o,String key){
    	CacheEntry e = cache.get(o.getProxyClass());
    	return e.keyToMethod.get(key);
    }

    public static String getKeyMethodName(OStore o){
    	CacheEntry e = cache.get(o.getProxyClass());
    	return e.keyMethodName;
    }

    public static String[] collectKeys(Class<? extends IStorable> cls){
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
        user.setUserid(((User)find.get(43)).getUserid());
        
    	user = (User)user.update();
    	    	
    	User del = (User)user.delete();
    	System.out.println(user.getUserid()+":"+user.getName());
    	
    	
    	Grant g = (Grant)OStore.newStorable(Grant.class);
    	g.setDescription("Hello");
    	g.setDomainid(5);
    	g.setRoleid(54654);
    	g.setUserid(23423);
    	g.write();
    }
}
