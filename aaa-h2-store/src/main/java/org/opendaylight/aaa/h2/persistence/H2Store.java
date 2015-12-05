/*
 * Copyright (c) 2015 Cisco Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.h2.persistence;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.clustering.AAAClusterListener;
import org.opendaylight.aaa.api.clustering.AAAClusterNode;
import org.opendaylight.aaa.api.clustering.AAAObjectEncoder;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.api.serializers.DomainSerializer;
import org.opendaylight.aaa.api.serializers.GrantSerializer;
import org.opendaylight.aaa.api.serializers.RoleSerializer;
import org.opendaylight.aaa.api.serializers.UserSerializer;
import org.opendaylight.aaa.h2.config.IdmLightConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2Store implements IIDMStore,AAAClusterListener{
    private static final Logger LOG = LoggerFactory.getLogger(H2Store.class);
    private static IdmLightConfig config = new IdmLightConfig();
    private DomainStore domainStore = new DomainStore();
    private UserStore userStore = new UserStore();
    private RoleStore roleStore = new RoleStore();
    private GrantStore grantStore = new GrantStore();
    private AAAClusterNode node=null;

    public H2Store() {
        try {
            node = new AAAClusterNode(this);
            new DomainSerializer();
            new RoleSerializer();
            new UserSerializer();
            new GrantSerializer();
        } catch (IOException e) {
            LOG.error("Failed to initialize cluster, will work only on local");
        }
    }

    @Override
    public void receivedObject(Object object, int op) {
        try {
            if (object instanceof Domain){
                switch(op){
                    case AAAObjectEncoder.OPERATION_WRITE:
                        domainStore.createDomain((Domain) object);
                        break;
                    case AAAObjectEncoder.OPERATION_UPDATE:
                        domainStore.putDomain((Domain)object);
                        break;
                    case AAAObjectEncoder.OPERATION_DELETE:
                        domainStore.deleteDomain(((Domain)object).getDomainid());
                        break;
                }
            }else
            if (object instanceof Role){
                switch(op){
                    case AAAObjectEncoder.OPERATION_WRITE:
                        roleStore.createRole((Role) object);
                        break;
                    case AAAObjectEncoder.OPERATION_UPDATE:
                        roleStore.putRole((Role)object);
                        break;
                    case AAAObjectEncoder.OPERATION_DELETE:
                        roleStore.deleteRole(((Role)object).getRoleid());
                        break;
                }
            }else
            if (object instanceof User){
                switch(op){
                    case AAAObjectEncoder.OPERATION_WRITE:
                        userStore.createUser((User) object);
                        break;
                    case AAAObjectEncoder.OPERATION_UPDATE:
                        userStore.putUser((User)object);
                        break;
                    case AAAObjectEncoder.OPERATION_DELETE:
                        userStore.deleteUser(((User)object).getUserid());
                        break;
                }
            }else
            if (object instanceof Grant){
                switch(op){
                    case AAAObjectEncoder.OPERATION_WRITE:
                        grantStore.createGrant((Grant) object);
                        break;
                    case AAAObjectEncoder.OPERATION_DELETE:
                        grantStore.deleteGrant(((Grant)object).getGrantid());
                        break;
                }
            }
        }catch(StoreException e){
            LOG.error("Failed to apply cluster command",e);
        }
    }

    public static Connection getConnection(Connection existingConnection) throws StoreException {
        Connection connection = existingConnection;
        try {
            if (existingConnection == null || existingConnection.isClosed()) {
                new org.h2.Driver();
                connection = DriverManager.getConnection(config.getDbPath(), config.getDbUser(),
                        config.getDbPwd());
            }
        } catch (Exception e) {
            throw new StoreException("Cannot connect to database server" + e);
        }

        return connection;
    }

    public static IdmLightConfig getConfig() {
        return config;
    }

    @Override
    public Domain writeDomain(Domain domain) throws IDMStoreException {
        try {
            Domain d = domainStore.createDomain(domain);
            if(node!=null){
                node.writeObject(d);
            }
            return d;
        } catch (StoreException e) {
            LOG.error("StoreException encountered while writing domain", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Domain readDomain(String domainid) throws IDMStoreException {
        try {
            return domainStore.getDomain(domainid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading domain", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Domain deleteDomain(String domainid) throws IDMStoreException {
        try {
            Domain d =  domainStore.deleteDomain(domainid);
            if(node!=null){
                node.deleteObject(d);
            }
            return d;
        } catch (StoreException e) {
            LOG.error("StoreException encountered while deleting domain", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Domain updateDomain(Domain domain) throws IDMStoreException {
        try {
            Domain d = domainStore.putDomain(domain);
            if(node!=null){
                node.updateObject(d);
            }
            return d;
        } catch (StoreException e) {
            LOG.error("StoreException encountered while updating domain", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Domains getDomains() throws IDMStoreException {
        try {
            return domainStore.getDomains();
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading domains", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Role writeRole(Role role) throws IDMStoreException {
        try{
            Role r = roleStore.createRole(role);
            if(node!=null){
                node.writeObject(role);
            }
            return r;
        }catch(StoreException e) {
            LOG.error("StoreException encountered while writing role", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Role readRole(String roleid) throws IDMStoreException {
        try {
            return roleStore.getRole(roleid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading role", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Role deleteRole(String roleid) throws IDMStoreException {
        try{
            Role r = roleStore.deleteRole(roleid);
            if(node!=null){
                node.deleteObject(r);
            }
            return r;
        }catch(StoreException e) {
            LOG.error("StoreException encountered while deleting role", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Role updateRole(Role role) throws IDMStoreException {
        try{
            Role r = roleStore.putRole(role);
            if(node!=null){
                node.updateObject(r);
            }
            return r;
        }catch(StoreException e) {
            LOG.error("StoreException encountered while updating role", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Roles getRoles() throws IDMStoreException {
        try {
            return roleStore.getRoles();
        } catch (StoreException e) {
            LOG.error("StoreException encountered while getting roles", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public User writeUser(User user) throws IDMStoreException {
        try {
            User u = userStore.createUser(user);
            if(node!=null){
                node.writeObject(u);
            }
            return u;
        } catch (StoreException e) {
            LOG.error("StoreException encountered while writing user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public User readUser(String userid) throws IDMStoreException {
        try {
            return userStore.getUser(userid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public User deleteUser(String userid) throws IDMStoreException {
        try {
            User u = userStore.deleteUser(userid);
            if(node!=null){
                node.deleteObject(u);
            }
            return u;
        } catch (StoreException e) {
            LOG.error("StoreException encountered while deleting user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public User updateUser(User user) throws IDMStoreException {
        try {
            User u =  userStore.putUser(user);
            if(node!=null){
                node.updateObject(u);
            }
            return u;
        } catch (StoreException e) {
            LOG.error("StoreException encountered while updating user", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Users getUsers(String username, String domain) throws IDMStoreException {
        try {
            return userStore.getUsers(username, domain);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading users", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Users getUsers() throws IDMStoreException {
        try {
            return userStore.getUsers();
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading users", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grant writeGrant(Grant grant) throws IDMStoreException {
        try {
            Grant g =  grantStore.createGrant(grant);
            if(node!=null){
                node.writeObject(g);
            }
            return g;
        } catch (StoreException e) {
            LOG.error("StoreException encountered while writing grant", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grant readGrant(String grantid) throws IDMStoreException {
        try {
            return grantStore.getGrant(grantid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while reading grant", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grant deleteGrant(String grantid) throws IDMStoreException {
        try {
            Grant g =  grantStore.deleteGrant(grantid);
            if(node!=null){
                node.deleteObject(g);
            }
            return g;
        } catch (StoreException e) {
            LOG.error("StoreException encountered while deleting grant", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grants getGrants(String domainid, String userid) throws IDMStoreException {
        try {
            return grantStore.getGrants(domainid, userid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while getting grants", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grants getGrants(String userid) throws IDMStoreException {
        try {
            return grantStore.getGrants(userid);
        } catch (StoreException e) {
            LOG.error("StoreException encountered while getting grants", e);
            throw new IDMStoreException(e);
        }
    }

    @Override
    public Grant readGrant(String domainid, String userid, String roleid) throws IDMStoreException {
        return readGrant(IDMStoreUtil.createGrantid(userid, domainid, roleid));
    }

    public static Domain createDomain(String domainName, boolean enable) throws StoreException {
        DomainStore ds = new DomainStore();
        Domain d = new Domain();
        d.setName(domainName);
        d.setEnabled(enable);
        return ds.createDomain(d);
    }

    public static User createUser(String name, String password, String domain, String description,
            String email, boolean enabled, String SALT) throws StoreException {
        UserStore us = new UserStore();
        User u = new User();
        u.setName(name);
        u.setDomainid(domain);
        u.setDescription(description);
        u.setEmail(email);
        u.setEnabled(enabled);
        u.setPassword(password);
        u.setSalt(SALT);
        return us.createUser(u);
    }

    public static Role createRole(String name, String domain, String description)
            throws StoreException {
        RoleStore rs = new RoleStore();
        Role r = new Role();
        r.setDescription(description);
        r.setName(name);
        r.setDomainid(domain);
        return rs.createRole(r);
    }

    public static Grant createGrant(String domain, String user, String role) throws StoreException {
        GrantStore gs = new GrantStore();
        Grant g = new Grant();
        g.setDomainid(domain);
        g.setRoleid(role);
        g.setUserid(user);
        return gs.createGrant(g);
    }
}
