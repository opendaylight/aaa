package org.opendaylight.aaa.idm.model;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.opendaylight.aaa.idm.persistence.IStorable;
import org.opendaylight.aaa.idm.persistence.OStore;

@XmlRootElement(name = "user")
public class JSUser implements User{
    
private User o = (User)OStore.newStorable(User.class);

    public static JSUser create(User o){
        JSUser r = new JSUser();
        r.o = o;
        return r;
    }
    

    @Override
    public String getName(){

        return o.getName();
    }
    @Override
    public void setName(String value){

        o.setName(value);
    }
    @Override
    public Integer getUserid(){

        return o.getUserid();
    }
    @Override
    public void setUserid(Integer value){

        o.setUserid(value);
    }
    @Override
    public void setDescription(String value){

        o.setDescription(value);
    }
    @Override
    public Boolean getEnabled(){

        return o.getEnabled();
    }
    @Override
    public void setEnabled(Boolean value){

        o.setEnabled(value);
    }
    @Override
    public void setEmail(String value){

        o.setEmail(value);
    }
    @Override
    public String getEmail(){

        return o.getEmail();
    }
    @Override
    public void setPassword(String value){

        o.setPassword(value);
    }
    @Override
    public String getPassword(){

        return o.getPassword();
    }
    @Override
    public String getDescription(){

        return o.getDescription();
    }
    @Override
    public IStorable get(){

        return o.get();
    }
    @Override
    public List<IStorable> find(){

        return o.find();
    }
    @Override
    public IStorable write(){

        return o.write();
    }
    @Override
    public IStorable delete(){

        return o.delete();
    }
    @Override
    public IStorable update(){

        return o.update();
    }
    @Override
    public List<IStorable> deleteAll(){

        return o.deleteAll();
    }
}
