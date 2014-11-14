package org.opendaylight.aaa.idm.model;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.opendaylight.aaa.idm.persistence.IStorable;
import org.opendaylight.aaa.idm.persistence.OStore;

@XmlRootElement(name = "domain")
public class JSDomain implements Domain{
    
private Domain o = (Domain)OStore.newStorable(Domain.class);

    public static JSDomain create(Domain o){
        JSDomain r = new JSDomain();
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
    public Boolean getEnabled(){

        return o.getEnabled();
    }
    @Override
    public void setEnabled(Boolean value){

        o.setEnabled(value);
    }
    @Override
    public Integer getDomainid(){

        return o.getDomainid();
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
