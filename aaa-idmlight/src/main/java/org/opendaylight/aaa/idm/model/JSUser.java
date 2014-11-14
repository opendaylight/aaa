package org.opendaylight.aaa.idm.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.aaa.idm.persistence.IStorable;
import org.opendaylight.aaa.idm.persistence.OStore;

@XmlRootElement(name = "user")
public class JSUser implements User{
    
	private User u = (User)OStore.newStorable(User.class);
	private Role r = (Role)OStore.newStorable(Role.class);
	
    public static JSUser create(User o){
        JSUser _u = new JSUser();
        _u.u = o;
        Grant g = (Grant)OStore.newStorable(Grant.class);
        if(o!=null && o.getUserid()!=null){
	        g.setUserid(o.getUserid());
	        Grant f = (Grant)g.get();
	        if(f!=null){
	        	_u.r.setRoleid(f.getRoleid());
	        	_u.r = (Role)_u.r.get();
	        }
        }
        return _u;
    }
    

    @Override
    public String getName(){

        return u.getName();
    }
    @Override
    public void setName(String value){

        u.setName(value);
    }
    @Override
    public Integer getUserid(){

        return u.getUserid();
    }
    @Override
    public void setUserid(Integer value){

        u.setUserid(value);
    }
    @Override
    public void setDescription(String value){

        u.setDescription(value);
    }
    @Override
    public Boolean getEnabled(){

        return u.getEnabled();
    }
    @Override
    public void setEnabled(Boolean value){

        u.setEnabled(value);
    }
    @Override
    public void setEmail(String value){

        u.setEmail(value);
    }
    @Override
    public String getEmail(){

        return u.getEmail();
    }
    @Override
    public void setPassword(String value){

        u.setPassword(value);
    }
    @Override
    public String getPassword(){

        return u.getPassword();
    }
    @Override
    public String getDescription(){

        return u.getDescription();
    }
    @Override
    public IStorable get(){

        return u.get();
    }
    @Override
    public List<IStorable> find(){

        return u.find();
    }
    @Override
    public IStorable write(){
        u = (User) u.write();    	
    	if(r.getRoleid()!=null){
    		Grant g = (Grant)OStore.newStorable(Grant.class);
    		g.setRoleid(r.getRoleid());
    		g.setUserid(u.getUserid());
    		g = (Grant)g.get();
    		if(g==null){
    			g = (Grant)OStore.newStorable(Grant.class);
    			g.setRoleid(r.getRoleid());
    			g = (Grant)g.get();
    			if(g!=null){
    				g.setGrantid(null);
	    			g.setUserid(u.getUserid());
	    			g.write();
    			}
    		}
    	}
    	return u;
    }
    @Override
    public IStorable delete(){

        return u.delete();
    }
    @Override
    public IStorable update(){

        return u.update();
    }
    @Override
    public List<IStorable> deleteAll(){

        return u.deleteAll();
    }
    
    public int getRoleID(){
    	return this.r.getRoleid();
    }
    
    public void setRoleID(int rid){
    	this.r.setRoleid(rid);
    }

    public String getRoleDescription(){
    	return this.r.getDescription();
    }    
    
    public void setRoleDescription(String desc){
    	this.r.setDescription(desc);
    }
    
    public String getRoleName(){
    	return this.r.getName();
    }
    
    public void setRoleName(String roleName){
    	this.r.setName(roleName);
    }    
}
