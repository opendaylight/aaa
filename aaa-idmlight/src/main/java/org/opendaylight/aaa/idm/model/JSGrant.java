package org.opendaylight.aaa.idm.model;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.opendaylight.aaa.idm.persistence.IStorable;
import org.opendaylight.aaa.idm.persistence.OStore;

@XmlRootElement(name = "grant")
public class JSGrant implements Grant{
    
private Grant o = (Grant)OStore.newStorable(Grant.class);

    public static JSGrant create(Grant o){
        JSGrant r = new JSGrant();
        r.o = o;
        return r;
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
    public Integer getRoleid(){

        return o.getRoleid();
    }
    @Override
    public void setRoleid(Integer value){

        o.setRoleid(value);
    }
    @Override
    public Integer getDomainid(){

        return o.getDomainid();
    }
    @Override
    public Integer getGrantid(){

        return o.getGrantid();
    }
    @Override
    public void setGrantid(Integer value){

        o.setGrantid(value);
    }
    @Override
    public String getDescription(){

        return o.getDescription();
    }
    @Override
    public void setDomainid(Integer value){

        o.setDomainid(value);
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
