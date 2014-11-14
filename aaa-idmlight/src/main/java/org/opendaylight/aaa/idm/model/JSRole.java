package org.opendaylight.aaa.idm.model;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.opendaylight.aaa.idm.persistence.IStorable;
import org.opendaylight.aaa.idm.persistence.OStore;

@XmlRootElement(name = "role")
public class JSRole implements Role{
    
private Role o = (Role)OStore.newStorable(Role.class);

    public static JSRole create(Role o){
        JSRole r = new JSRole();
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
    public void setDescription(String value){

        o.setDescription(value);
    }
    @Override
    public Integer getRoleid(){

        return o.getRoleid();
    }
    @Override
    public void setRoleid(Integer value){

        o.setRoleid(value);
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
