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

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.Authentication;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Domain;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.DomainKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Grant;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.GrantKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.Role;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.RoleKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.User;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.authentication.UserKey;
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
public class IDMMDSALStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(IDMMDSALStore.class);
    private final DataBroker dataBroker;
    /**
     * Reflection elements data structure maps to avoid code duplication for different MDSAL objects.
     * Each MDSAL DataObject introduced to the store must add CRUD operations that wrap the reflection code for tractability.
     * Hence, making it easy to change the model and introduce new elements.
     * Performance consideration of reflection is neglectable here as the actual persistence operation is 10 times or more slower...
     */
    private static final Map<Class<?>, Class<?>> type2Builder = new HashMap<Class<?>, Class<?>>();
    private static final Map<Class<?>, Method[]> builderClass2SetMethods = new HashMap<Class<?>, Method[]>();
    private static final Map<Class<?>,Map<String, Method>> setMethodName2GetMethod = new HashMap<Class<?>,Map<String, Method>>();
    private static final Map<Class<?>, Method> builderClass2BuildMethod = new HashMap<Class<?>, Method>();
    private static final Map<Class<?>, Constructor<?>> builderClassToKeyConstructorClass = new HashMap<Class<?>, Constructor<?>>();
    private static final String BUILDER_CLASS_SUFFIX = "Builder";
    private static final String KEY_SUFFIX = "Key";
    private static final String SET_METHOD_PREFIX = "set";
    private static final String IS_METHOD_PREFIX = "is";
    private static final String GET_METHOD_PREFIX = "get";
    private static final String SET_KEY_METHOD_NAME = "setKey";
    
    public IDMMDSALStore(DataBroker dataBroker){
        this.dataBroker = dataBroker;
    }

    private Class<?> getBuilderClass(final DataObject dataObject) throws Exception{
        Class<?> builderClass = type2Builder.get(dataObject.getImplementedInterface());
        if(builderClass!=null)
            return builderClass;
        builderClass = this.getClass().getClassLoader().loadClass(dataObject.getImplementedInterface().getName()+BUILDER_CLASS_SUFFIX);
        type2Builder.put(dataObject.getImplementedInterface(), builderClass);
        Method methods[] = builderClass.getMethods();
        String keyClassName = dataObject.getImplementedInterface().getName()+KEY_SUFFIX;
        final Class<?> keyClass = this.getClass().getClassLoader().loadClass(keyClassName);
        builderClassToKeyConstructorClass.put(builderClass,keyClass.getConstructor(new Class[]{String.class}));
        final List<Method> setMethods = new ArrayList<Method>();
        final Map<String,Method> set2get = new HashMap<String,Method>();

        for(Method m:methods){
            if(m.getName().startsWith(SET_METHOD_PREFIX)){
                setMethods.add(m);
                if(Boolean.class.equals(m.getParameterTypes()[0]) || boolean.class.equals(m.getParameterTypes()[0])){
                    set2get.put(m.getName(),dataObject.getImplementedInterface().getMethod(IS_METHOD_PREFIX+m.getName().substring(3), (Class[])null));
                }else{
                    set2get.put(m.getName(),dataObject.getImplementedInterface().getMethod(GET_METHOD_PREFIX+m.getName().substring(3), (Class[])null));
                }
            }
        }
        setMethodName2GetMethod.put(builderClass, set2get);
        builderClass2SetMethods.put(builderClass, (Method[])setMethods.toArray(new Method[setMethods.size()]));
        builderClass2BuildMethod.put(builderClass, builderClass.getMethod("build",(Class[])null));
        return builderClass;
    }

    private InstanceIdentifier<?> createInstanceIdentifier(final Object keyObject){
        if(keyObject instanceof DomainKey){
            return InstanceIdentifier.create(Authentication.class).child(Domain.class,(DomainKey)keyObject);
        }else
        if(keyObject instanceof RoleKey){
            return InstanceIdentifier.create(Authentication.class).child(Role.class,(RoleKey)keyObject);
        }else
        if(keyObject instanceof UserKey){
            return InstanceIdentifier.create(Authentication.class).child(User.class,(UserKey)keyObject);
        }else
        if(keyObject instanceof GrantKey){
            return InstanceIdentifier.create(Authentication.class).child(Grant.class,(GrantKey)keyObject);
        }
        return null;
    }

    private DataObject writeDataObject(final DataObject dataObject,final String keyValue,final String idMethodName) throws Exception{
        final Class<?> builderClass = getBuilderClass(dataObject);
        final Object builder = builderClass.newInstance();
        final Method methods[] = builderClass2SetMethods.get(builderClass);
        Object keyObject = null;
        final Map<String,Method> set2get = setMethodName2GetMethod.get(builderClass);

        for(Method m:methods){
            if(m.getName().equals(idMethodName)){
                m.invoke(builder, new Object[]{keyValue});
            }else
            if(m.getName().equals(SET_KEY_METHOD_NAME)){
                Constructor<?> constructor = builderClassToKeyConstructorClass.get(builderClass);
                keyObject = constructor.newInstance(new Object[]{keyValue});
                m.invoke(builder, new Object[]{keyObject});
            }else
            if(m.getName().startsWith(SET_METHOD_PREFIX)){
                try{
                    m.invoke(builder, new Object[]{set2get.get(m.getName()).invoke(dataObject,(Object[])null)});
                }catch(Exception err){
                    LOGGER.error("Failed to set method value "+m.getName(),err);
                }
            }
        }
        InstanceIdentifier ID = createInstanceIdentifier(keyObject);
        WriteTransaction writeOnlyTransaction = dataBroker.newWriteOnlyTransaction();
        DataObject objectToCreate = (DataObject)builderClass2BuildMethod.get(builderClass).invoke(builder, (Object[])null);
        writeOnlyTransaction.put(LogicalDatastoreType.CONFIGURATION, ID, objectToCreate);
        writeOnlyTransaction.submit();
        if(LOGGER.isInfoEnabled()){
            LOGGER.info("Written object "+keyObject+" to datastore.");
        }
        return objectToCreate;
    }

    private DataObject readDataObject(final Object keyObject){
        Preconditions.checkNotNull(keyObject);
        InstanceIdentifier ID = createInstanceIdentifier(keyObject);
        ReadOnlyTransaction newReadOnlyTransaction = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<?>, ReadFailedException> read = (CheckedFuture<Optional<?>, ReadFailedException>)newReadOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, ID);

        if(read==null){
            LOGGER.error("Empty result on a read DataObject "+keyObject+" transaction.");
            return null;
        }

        try {
            if(read.get()==null){
                LOGGER.error("Empty Optional on a read DataObject "+keyObject+" transaction.");
                return null;
            }
            if(read.get().get()==null || !read.get().isPresent()){
                LOGGER.error("Could not find DataObject with "+keyObject+" Key.");
                return null;
            }
            return (DataObject)read.get().get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error while executing read domain transaction.",e);
            return null;
        }
    }

    private DataObject deleteDataObject(final Object keyObject){
        Preconditions.checkNotNull(keyObject);
        DataObject result = readDataObject(keyObject);
        if(result==null)
            return null;

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
            LOGGER.error("No DataObject with the ID "+keyObject+" was found");
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
        LOGGER.info("Updated object "+keyObject+" to datastore.");
        return objectToCreate;
    }

    //Domain methods
    public Domain writeDomain(Domain domain) throws IDMStoreException {
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(domain.getName());
        Preconditions.checkNotNull(domain.isEnabled());
        try{
            return (Domain)writeDataObject(domain, domain.getName(),"setDomainid");
        }catch(Exception e){
            throw new IDMStoreException(e.getMessage());
        }
    }
    public Domain readDomain(String domainid){
        Preconditions.checkNotNull(domainid);
        return (Domain)readDataObject(new DomainKey(domainid));
    }
    public Domain deleteDomain(String domainid){
        Preconditions.checkNotNull(domainid);
        return (Domain)deleteDataObject(new DomainKey(domainid));
    }
    public Domain updateDomain(Domain domain) throws IDMStoreException {
        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(domain.getDomainid());
        try{
            return (Domain)updateDataObject(domain, domain.getDomainid(), "setDomainid");
        }catch(Exception e){
            throw new IDMStoreException(e.getMessage());
        }
    }

    public List<Domain> getAllDomains(){
        InstanceIdentifier<Authentication> id = InstanceIdentifier.create(Authentication.class);
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Authentication>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION, id);
        if(read!=null)
            return null;

        try{
            if(read.get()==null)
                return null;
            if(read.get().isPresent()){
                Authentication auth = read.get().get();
                return auth.getDomain();
            }
        }catch(Exception err){
            LOGGER.error("Failed to read domains",err);
        }
        return null;
    }

    public List<Role> getAllRoles(){
        InstanceIdentifier<Authentication> id = InstanceIdentifier.create(Authentication.class);
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Authentication>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION, id);
        if(read!=null)
            return null;

        try{
            if(read.get()==null)
                return null;
            if(read.get().isPresent()){
                Authentication auth = read.get().get();
                return auth.getRole();
            }
        }catch(Exception err){
            LOGGER.error("Failed to read domains",err);
        }
        return null;
    }

    public List<User> getAllUsers(){
        InstanceIdentifier<Authentication> id = InstanceIdentifier.create(Authentication.class);
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Authentication>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION, id);
        if(read!=null)
            return null;

        try{
            if(read.get()==null)
                return null;
            if(read.get().isPresent()){
                Authentication auth = read.get().get();
                return auth.getUser();
            }
        }catch(Exception err){
            LOGGER.error("Failed to read domains",err);
        }
        return null;
    }

    public List<Grant> getAllGrants(){
        InstanceIdentifier<Authentication> id = InstanceIdentifier.create(Authentication.class);
        ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<Authentication>, ReadFailedException> read = rot.read(LogicalDatastoreType.CONFIGURATION, id);
        if(read!=null)
            return null;

        try{
            if(read.get()==null)
                return null;
            if(read.get().isPresent()){
                Authentication auth = read.get().get();
                return auth.getGrant();
            }
        }catch(Exception err){
            LOGGER.error("Failed to read domains",err);
        }
        return null;
    }

    //Role methods
    public Role writeRole(Role role) throws IDMStoreException {
        Preconditions.checkNotNull(role);
        Preconditions.checkNotNull(role.getName());
        Preconditions.checkNotNull(role.getDomainid());
        Preconditions.checkNotNull(readDataObject(new DomainKey(role.getDomainid())));
        try{
            return (Role)writeDataObject(role, IDMStoreUtil.createRoleid(role.getName(),role.getDomainid()),"setRoleid");
        }catch(Exception e){
            throw new IDMStoreException(e.getMessage());
        }
    }
    public Role readRole(String roleid){
        Preconditions.checkNotNull(roleid);
        return (Role)readDataObject(new RoleKey(roleid));
    }
    public Role deleteRole(String roleid){
        Preconditions.checkNotNull(roleid);
        return (Role)deleteDataObject(new RoleKey(roleid));
    }
    public Role updateRole(Role role) throws IDMStoreException {
        Preconditions.checkNotNull(role);
        Preconditions.checkNotNull(role.getRoleid());
        try{
            return (Role)updateDataObject(role, role.getRoleid(), "setRoleid");
        }catch(Exception e){
            throw new IDMStoreException(e.getMessage());
        }
    }

    //User methods
    public User writeUser(User user) throws IDMStoreException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(user.getName());
        Preconditions.checkNotNull(user.getDomainid());
        Preconditions.checkNotNull(readDataObject(new DomainKey(user.getDomainid())));
        try{
            return (User)writeDataObject(user, IDMStoreUtil.createUserid(user.getName(),user.getDomainid()),"setUserid");
        }catch(Exception e){
            throw new IDMStoreException(e.getMessage());
        }
    }
    public User readUser(String userid){
        Preconditions.checkNotNull(userid);
        return (User)readDataObject(new UserKey(userid));
    }
    public User deleteUser(String userid){
        Preconditions.checkNotNull(userid);
        return (User)deleteDataObject(new UserKey(userid));
    }
    public User updateUser(User user) throws IDMStoreException {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(user.getUserid());
        try{
            return (User)updateDataObject(user, user.getUserid(), "setUserid");
        }catch(Exception e){
            throw new IDMStoreException(e.getMessage());
        }
    }

    //Grant methods
    public Grant writeGrant(Grant grant) throws IDMStoreException {
        Preconditions.checkNotNull(grant);
        Preconditions.checkNotNull(grant.getDomainid());
        Preconditions.checkNotNull(grant.getUserid());
        Preconditions.checkNotNull(grant.getRoleid());
        Preconditions.checkNotNull(readDataObject(new DomainKey(grant.getDomainid())));
        Preconditions.checkNotNull(readDataObject(new UserKey(grant.getUserid())));
        Preconditions.checkNotNull(readDataObject(new RoleKey(grant.getRoleid())));
        try{
            return (Grant)writeDataObject(grant, IDMStoreUtil.createGrantid(grant.getUserid(),grant.getDomainid(),grant.getRoleid()),"setGrantid");
        }catch(Exception e){
            throw new IDMStoreException(e.getMessage());
        }
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
