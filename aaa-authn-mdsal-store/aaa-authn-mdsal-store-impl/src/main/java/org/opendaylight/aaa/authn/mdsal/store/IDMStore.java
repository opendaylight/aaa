/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.authn.mdsal.store;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.AAA;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.Domain;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.DomainKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.Grant;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.GrantKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.Role;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.RoleKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.User;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.aaa.UserKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;

/**
 * @author Sharon Aicler - saichler@cisco.com
 *
 */
public class IDMStore {

    private static final Logger log = LoggerFactory.getLogger(IDMStore.class);
    private final DataBroker dataBroker;
    private static Map<Class<?>, Class<?>> type2Builder = new HashMap<Class<?>, Class<?>>();
    private static Map<Class<?>, Method[]> builderClass2SetMethods = new HashMap<Class<?>, Method[]>();
    private static Map<Class<?>,Map<String, Method>> setMethodName2GetMethod = new HashMap<Class<?>,Map<String, Method>>();
    private static Map<Class<?>, Method> builderClass2BuildMethod = new HashMap<Class<?>, Method>();
    private static Map<Class<?>, Constructor<?>> builderClassToKeyConstructorClass = new HashMap<Class<?>, Constructor<?>>();

    public IDMStore(DataBroker dataBroker){
        this.dataBroker = dataBroker;
    }

    private Class<?> getBuilderClass(DataObject dataObject) throws Exception{
        Class<?> builderClass = type2Builder.get(dataObject.getImplementedInterface());
        if(builderClass!=null)
            return builderClass;
        builderClass = this.getClass().getClassLoader().loadClass(dataObject.getImplementedInterface().getName()+"Builder");
        type2Builder.put(dataObject.getImplementedInterface(), builderClass);
        Method methods[] = builderClass.getMethods();
        String keyClassName = dataObject.getImplementedInterface().getName()+"Key";
        Class<?> keyClass = this.getClass().getClassLoader().loadClass(keyClassName);
        builderClassToKeyConstructorClass.put(builderClass,keyClass.getConstructor(new Class[]{String.class}));
        List<Method> setMethods = new ArrayList<Method>();
        Map<String,Method> set2get = new HashMap<String,Method>();

        for(Method m:methods){
            if(m.getName().startsWith("set")){
                setMethods.add(m);
                if(Boolean.class.equals(m.getParameterTypes()[0]) || boolean.class.equals(m.getParameterTypes()[0])){
                    set2get.put(m.getName(),dataObject.getImplementedInterface().getMethod("is"+m.getName().substring(3), (Class[])null));
                }else{
                    set2get.put(m.getName(),dataObject.getImplementedInterface().getMethod("get"+m.getName().substring(3), (Class[])null));
                }
            }
        }
        setMethodName2GetMethod.put(builderClass, set2get);
        builderClass2SetMethods.put(builderClass, (Method[])setMethods.toArray(new Method[setMethods.size()]));
        builderClass2BuildMethod.put(builderClass, builderClass.getMethod("build",(Class[])null));
        return builderClass;
    }

    private InstanceIdentifier<?> createInstanceIdentifier(Object keyObject){
        if(keyObject instanceof DomainKey){
            return InstanceIdentifier.create(AAA.class).child(Domain.class,(DomainKey)keyObject);
        }else
        if(keyObject instanceof RoleKey){
            return InstanceIdentifier.create(AAA.class).child(Role.class,(RoleKey)keyObject);
        }else
        if(keyObject instanceof UserKey){
            return InstanceIdentifier.create(AAA.class).child(User.class,(UserKey)keyObject);
        }else
        if(keyObject instanceof GrantKey){
            return InstanceIdentifier.create(AAA.class).child(Grant.class,(GrantKey)keyObject);
        }
        return null;
    }

    private DataObject writeDataObject(DataObject dataObject,String keyValue,String idMethodName) throws Exception{
        Class<?> builderClass = getBuilderClass(dataObject);
        Object builder = builderClass.newInstance();
        Method methods[] = builderClass2SetMethods.get(builderClass);
        Object keyObject = null;
        Map<String,Method> set2get = setMethodName2GetMethod.get(builderClass);

        for(Method m:methods){
            if(m.getName().equals(idMethodName)){
                m.invoke(builder, new Object[]{keyValue});
            }else
            if(m.getName().equals("setKey")){
                Constructor<?> constructor = builderClassToKeyConstructorClass.get(builderClass);
                keyObject = constructor.newInstance(new Object[]{keyValue});
                m.invoke(builder, new Object[]{keyObject});
            }else
            if(m.getName().startsWith("set")){
                try{
                    m.invoke(builder, new Object[]{set2get.get(m.getName()).invoke(dataObject,(Object[])null)});
                }catch(Exception err){
                    err.printStackTrace();
                }
            }
        }
        InstanceIdentifier ID = createInstanceIdentifier(keyObject);
        WriteTransaction writeOnlyTransaction = dataBroker.newWriteOnlyTransaction();
        DataObject objectToCreate = (DataObject)builderClass2BuildMethod.get(builderClass).invoke(builder, (Object[])null);
        writeOnlyTransaction.put(LogicalDatastoreType.CONFIGURATION, ID, objectToCreate);
        writeOnlyTransaction.submit();
        log.info("Written object "+keyObject+" to datastore.");
        return objectToCreate;
    }

    private DataObject readDataObject(Object keyObject){
        Preconditions.checkNotNull(keyObject);
        InstanceIdentifier ID = createInstanceIdentifier(keyObject);
        ReadOnlyTransaction newReadOnlyTransaction = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<?>, ReadFailedException> read = (CheckedFuture<Optional<?>, ReadFailedException>)newReadOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, ID);

        if(read==null){
            log.error("Empty result on a read DataObject "+keyObject+" transaction.");
            return null;
        }

        try {
            if(read.get()==null){
                log.error("Empty Optional on a read DataObject "+keyObject+" transaction.");
                return null;
            }
            if(read.get().get()==null || !read.get().isPresent()){
                log.error("Could not find DataObject with "+keyObject+" Key.");
                return null;
            }
            return (DataObject)read.get().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while executing read domain transaction.",e);
            return null;
        }
    }

    private DataObject deleteDataObject(Object keyObject){
        Preconditions.checkNotNull(keyObject);
        DataObject result = readDataObject(keyObject);
        if(result==null)
            return null;
        //@TODO - add here code to delete all users+roles+grants of this domain
        InstanceIdentifier ID = createInstanceIdentifier(keyObject);
        WriteTransaction newWriteOnlyTransaction = dataBroker.newWriteOnlyTransaction();
        newWriteOnlyTransaction.delete(LogicalDatastoreType.CONFIGURATION, ID);
        newWriteOnlyTransaction.submit();
        return result;
    }

    private DataObject updateDataObject(DataObject dataObject,String keyValue,String idMethodName) throws Exception{
        Class<?> builderClass = getBuilderClass(dataObject);
        Constructor<?> constructor = builderClassToKeyConstructorClass.get(builderClass);
        Object keyObject = constructor.newInstance(new Object[]{keyValue});

        DataObject current = readDataObject(keyObject);
        if(current==null){
            log.error("No DataObject with the ID "+keyObject+" was found");
            return null;
        }

        Object builder = builderClass.newInstance();
        Method methods[] = builderClass2SetMethods.get(builderClass);
        Map<String,Method> set2get = setMethodName2GetMethod.get(builderClass);

        for(Method m:methods){
            if(m.getName().equals(idMethodName)){
                m.invoke(builder, new Object[]{keyValue});
            }else
            if(m.getName().equals("setKey")){
                m.invoke(builder, new Object[]{keyObject});
            }else
            if(m.getName().startsWith("set")){
                Object value = set2get.get(m.getName()).invoke(dataObject,(Object[])null);
                if(value==null)
                    value = set2get.get(m.getName()).invoke(current,(Object[])null);
                m.invoke(builder, new Object[]{value});
            }
        }

        InstanceIdentifier ID = createInstanceIdentifier(keyObject);
        WriteTransaction newWriteOnlyTransaction = dataBroker.newWriteOnlyTransaction();
        DataObject objectToCreate = (DataObject)builderClass2BuildMethod.get(builderClass).invoke(builder, (Object[])null);
        newWriteOnlyTransaction.put(LogicalDatastoreType.CONFIGURATION, ID, objectToCreate);
        newWriteOnlyTransaction.submit();
        log.info("Updated object "+keyObject+" to datastore.");
        return objectToCreate;
    }

    //Domain methods
    public Domain writeDomain(Domain domain) throws Exception{
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(domain.getName());
        Preconditions.checkNotNull(domain.isEnabled());
        return (Domain)writeDataObject(domain, domain.getName(),"setDomainid");
    }
    public Domain readDomain(String domainid){
        Preconditions.checkNotNull(domainid);
        return (Domain)readDataObject(new DomainKey(domainid));
    }
    public Domain deleteDomain(String domainid){
        Preconditions.checkNotNull(domainid);
        return (Domain)deleteDataObject(new DomainKey(domainid));
    }
    public Domain updateDomain(Domain domain) throws Exception{
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(domain.getDomainid());
        return (Domain)updateDataObject(domain, domain.getDomainid(), "setDomainid");
    }

    //Role methods
    public Role writeRole(Role role) throws Exception{
        Preconditions.checkNotNull(role);
        Preconditions.checkNotNull(role.getName());
        Preconditions.checkNotNull(role.getDomainid());
        Preconditions.checkNotNull(readDataObject(new DomainKey(role.getDomainid())));
        return (Role)writeDataObject(role, role.getName()+"@"+role.getDomainid(),"setRoleid");
    }
    public Role readRole(String roleid){
        Preconditions.checkNotNull(roleid);
        return (Role)readDataObject(new RoleKey(roleid));
    }
    public Role deleteRole(String roleid){
        Preconditions.checkNotNull(roleid);
        return (Role)deleteDataObject(new RoleKey(roleid));
    }
    public Role updateRole(Role role) throws Exception{
        Preconditions.checkNotNull(role);
        Preconditions.checkNotNull(role.getRoleid());
        return (Role)updateDataObject(role, role.getRoleid(), "setRoleid");
    }

    //User methods
    public User writeUser(User user) throws Exception{
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(user.getName());
        Preconditions.checkNotNull(user.getDomainid());
        Preconditions.checkNotNull(readDataObject(new DomainKey(user.getDomainid())));
        return (User)writeDataObject(user, user.getName()+"@"+user.getDomainid(),"setUserid");
    }
    public User readUser(String userid){
        Preconditions.checkNotNull(userid);
        return (User)readDataObject(new UserKey(userid));
    }
    public User deleteUser(String userid){
        Preconditions.checkNotNull(userid);
        return (User)deleteDataObject(new UserKey(userid));
    }
    public User updateUser(User user) throws Exception{
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(user.getUserid());
        return (User)updateDataObject(user, user.getUserid(), "setUserid");
    }

    //Grant methods
    public Grant writeGrant(Grant grant) throws Exception{
        Preconditions.checkNotNull(grant);
        Preconditions.checkNotNull(grant.getDomainid());
        Preconditions.checkNotNull(grant.getUserid());
        Preconditions.checkNotNull(grant.getRoleid());
        Preconditions.checkNotNull(readDataObject(new DomainKey(grant.getDomainid())));
        Preconditions.checkNotNull(readDataObject(new UserKey(grant.getUserid())));
        Preconditions.checkNotNull(readDataObject(new RoleKey(grant.getRoleid())));
        return (Grant)writeDataObject(grant, grant.getDomainid()+"@"+grant.getUserid()+"@"+grant.getRoleid(),"setGrantid");
    }
    public Grant readGrant(String grantid){
        Preconditions.checkNotNull(grantid);
        return (Grant)readDataObject(new GrantKey(grantid));
    }
    public Grant deleteGrant(String grantid){
        Preconditions.checkNotNull(grantid);
        return (Grant)deleteDataObject(new GrantKey(grantid));
    }
}
