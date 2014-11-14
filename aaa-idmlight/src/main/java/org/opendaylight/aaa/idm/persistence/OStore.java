package org.opendaylight.aaa.idm.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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

import org.opendaylight.aaa.idm.model.Domain;
import org.opendaylight.aaa.idm.model.Grant;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

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
    	log("Loading Data Store");
    	IOStore s = null;
    	try{
    		InputStream sin = OStore.class.getClassLoader().getResourceAsStream("store.ini");    		 
    		BufferedReader in = new BufferedReader(new InputStreamReader(sin));
    		String line1 = in.readLine().trim();
    		String line2 = in.readLine().trim();    		
    		in.close();
    		int index = line1.indexOf("class=");
    		String className = line1.substring(index+6).trim();
    		log("Class Name="+className);
    		index = line2.indexOf("jars=");
    		String driverJars = line2.substring(index+5).trim();
    		log("jars locations="+driverJars);    		
    		File jars = new File(driverJars);
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
    		log(err);
    	}
    	return null;
    }
    
    public boolean doEquals(Object o2){
    	return compare(this,(OStore)Proxy.getInvocationHandler(o2));
    }
    
    @Override
    public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
    	try{
        String mname = arg1.getName();
        if(mname.equals("equals")){
        	return doEquals(arg2[0]);
        }else
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
    	}catch(Exception err){
    		log(err);
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
                }catch(Exception err){log(err);}
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
    	OStore.createJSWrapper(User.class,"./src/main/java/");
    	OStore.createJSWrapper(Role.class,"./src/main/java/");
    	OStore.createJSWrapper(Domain.class,"./src/main/java/");
    	OStore.createJSWrapper(Grant.class,"./src/main/java/");    	
    	/*
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
    	
    	
    	Domain d = (Domain)OStore.newStorable(Domain.class);
    	d.dofo();
    	*/
    	/*
    	User userUser = (User)OStore.newStorable(User.class);
    	System.out.println(userUser.getClass());
        userUser.setEnabled(true);
        userUser.setName("user");
        userUser.setDescription("user user");
        userUser.setEmail("");
        userUser.setPassword(MD5Calculator.getMD5("user"));
        //userUser = userStore.createUser(userUser);
        User uTemp = (User)userUser.get();
        if(uTemp==null){
       	 userUser = (User)userUser.write();
        }else
       	 userUser = uTemp;
    	System.out.println(userUser.getUserid());
    	*/
    }
    
    private static PrintWriter out = null;
    public static synchronized void log(String str){
		try{
			out = new PrintWriter(new FileOutputStream("/tmp/ostore.log",true));
		}catch(Exception err){
			err.printStackTrace();
		}
    	out.println(str);
    	out.close();
    }
    public static synchronized void log(Exception e){
		try{
			out = new PrintWriter(new FileOutputStream("/tmp/ostore.log",true));
		}catch(Exception err){
			err.printStackTrace();
		}
    	e.printStackTrace(out);    
    }
    
    public static void fromJSon(JsonNode node,OStore o){
    	Method methods[] = o.proxyClass.getMethods();
    	for(Method m:methods){
    		if(m.getName().startsWith("set")){
    			String name = m.getName().substring(3);
    			String sValue = node.get(name).asText();
    			if(m.getParameterTypes()[0].equals(String.class)){
    				o.data.put(name, sValue);
    			}else
    			if(m.getParameterTypes()[0].equals(int.class) || m.getParameterTypes().equals(Integer.class)){
    				o.data.put(name, Integer.parseInt(sValue));
    			}else
        		if(m.getParameterTypes()[0].equals(boolean.class) || m.getParameterTypes().equals(Boolean.class)){
        			o.data.put(name, Boolean.parseBoolean(sValue));
        		}
    		}
    	}
    }
    
    public static void toJSon(JsonGenerator jgen,IStorable oo) throws IOException,JsonProcessingException{
    	OStore o = (OStore)Proxy.getInvocationHandler(oo);
		jgen.writeStartObject();
		for(Map.Entry<String, Object> entry:o.data.entrySet()){
			jgen.writeStringField(entry.getKey(),""+entry.getValue());
		}
		jgen.writeEndObject();
    }
    
    public static void createJSWrapper(Class<? extends IStorable> cls,String dir){
    	String jsClassName = cls.getPackage().getName()+".JS"+cls.getSimpleName();
    	String jsFileName = replace(jsClassName,'.','/')+".java";
    	File jsFile = new File(jsFileName);
    	try{
    		FileOutputStream out = new FileOutputStream(dir+jsFile);
    		append("package "+cls.getPackage().getName()+";",0,out);
    		append("\nimport java.util.List;",0,out);
    		append("import javax.xml.bind.annotation.XmlRootElement;",0,out);
    		append("import org.opendaylight.aaa.idm.persistence.IStorable;",0,out);
    		append("import org.opendaylight.aaa.idm.persistence.OStore;",0,out);

    		append("\n@XmlRootElement(name = \""+cls.getSimpleName().toLowerCase()+"\")",0,out);
    		append("public class JS"+cls.getSimpleName()+" implements "+cls.getSimpleName()+"{",0,out);
    		append("\nprivate "+cls.getSimpleName()+" o = ("+cls.getSimpleName()+")OStore.newStorable("+cls.getSimpleName()+".class);\n",1,out);
    		
    		append("public static JS"+cls.getSimpleName()+" create("+cls.getSimpleName()+" o){",1,out);
    		append("JS"+cls.getSimpleName()+" r = new JS"+cls.getSimpleName()+"();",2,out);
    		append("r.o = o;",2,out);
    		append("return r;",2,out);
    		append("}",1,out);    		
    		append("\n",1,out);
    		for(Method m:cls.getMethods()){
    			append("@Override",1,out);
    			String mTitle = "public ";
    			if(List.class.isAssignableFrom(m.getReturnType())){
    				mTitle+="List<IStorable> "+m.getName()+"(";    			
    			}else
    				mTitle+=m.getReturnType().getSimpleName()+" "+m.getName()+"(";
    			boolean isFirst = true;
    			for(Class c:m.getParameterTypes()){
    				if(!isFirst)
    					mTitle+=", ";
    				isFirst = false;
    				mTitle+=c.getSimpleName()+" value";
    			}
    			mTitle+="){\n";
    			append(mTitle,1,out);
    			if(m.getReturnType().equals(void.class)){
    				append("o."+m.getName()+"(value);",2,out);
    			}else{
    				append("return o."+m.getName()+"();",2,out);
    			}
    			append("}",1,out);
    		}
    		append("}",0,out);
    	}catch(Exception err){
    		err.printStackTrace();
    	}
    }
    
    public static boolean compare(OStore o1,OStore o2){
    	if(o1==null && o2!=null)
    		return false;
    	if(o1!=null && o2==null)
    		return false;
    	if(o1==null && o2==null)
    		return true;
    	
    	Map<String,Object> m1 = o1.getData();
    	Map<String,Object> m2 = o2.getData();
    	
    	if(m1.size()!=m2.size())
    		return false;
    	
    	for(Map.Entry<String, Object> e:m1.entrySet()){
    		Object v1 = e.getValue();
    		Object v2 = m2.get(e.getKey());
    		if(v1==null && v2!=null)
    			return false;
    		if(v1!=null && v2==null)
    			return false;
    		if(v1==null && v2==null)
    			return false;
    		if(!v1.equals(v2)){
    			return false;
    		}
    	}
    	return true;
    }
    
    public static void append(String str,int level,OutputStream out) throws IOException{
    	for(int i=0;i<level*4;i++){
    		out.write(" ".getBytes());
    	}
    	out.write(str.getBytes());
    	out.write("\n".getBytes());
    }
    
    public static String replace(String str,char that,char withThis){
    	StringBuffer buff = new StringBuffer();
    	for(int i=0;i<str.length();i++){
    		if(str.charAt(i)==that){
    			buff.append(withThis);
    		}else
    			buff.append(str.charAt(i));
    	}
    	return buff.toString();
    }
}
