/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cassandra.persistence;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.exceptions.InvalidQueryException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.SHA256Calculator;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author saichler@gmail.com
 */
public class CassandraStore implements IIDMStore{
    private static final Logger LOG = LoggerFactory.getLogger(CassandraStore.class);
    private DomainStore domainStore;
    private UserStore userStore;
    private RoleStore roleStore;
    private GrantStore grantStore;
    private Cluster cluster = null;
    private Session session = null;
    private boolean isMainNode = true;
    private String host = null;
    private int replication_factor = 1;

    public CassandraStore() {
        try {
            domainStore = new DomainStore(this);
            roleStore = new RoleStore(this);
            userStore = new UserStore(this);
            grantStore = new GrantStore(this);
        } catch (NoSuchMethodException e) {
            LOG.error("Failed to instantiate stores",e);
        }
    }

    public CassandraStore(Cluster c,Session s){
        this();
        this.cluster = c;
        this.session = s;
    }

    public Session getSession() throws IOException {
        if (session == null) {
            synchronized (this) {
                this.host = CassandraConfig.getInstance().getHost();
                this.isMainNode = CassandraConfig.getInstance().isMainNode();
                this.replication_factor = CassandraConfig.getInstance().getReplicationFactor();
                LOG.info("Trying to work with {}, Which main node is set to={}",this.host,this.isMainNode);
                cluster = Cluster.builder().addContactPoint(host).build();

                // Try 5 times to connect to cassandra with a 5 seconds delay
                // between each try
                for (int index = 0; index < 5; index++) {
                    try {
                        session = cluster.connect("aaa");
                        return session;
                    } catch (InvalidQueryException err) {
                        try {
                            LOG.info("Failed to get aaa keyspace...");
                            if (this.isMainNode) {
                                LOG.info("This is the main node, trying to create keyspace and tables...");
                                session = cluster.connect();
                                session.execute("CREATE KEYSPACE aaa WITH replication "
                                        + "= {'class':'SimpleStrategy', 'replication_factor':"+replication_factor+"};");
                                session = cluster.connect("aaa");
                                return session;
                            }
                        } catch (Exception err2) {
                            LOG.error("Failed to create keyspace & tables, will retry in 5 seconds...",err2);
                        }
                    }
                    LOG.info("Sleeping for 5 seconds...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        LOG.error("Interrupted",e);
                    }
                }
            }
        }
        return session;
    }

    public boolean doesTableExist(String tableName){
        KeyspaceMetadata ks = cluster.getMetadata().getKeyspace("aaa");
        TableMetadata table = ks.getTable(tableName);
        if(table==null) {
            return false;
        }
        return true;
    }

    @Override
    public Domain writeDomain(Domain domain) throws IDMStoreException {
        try {
            return domainStore.createElement(domain);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Domain readDomain(String domainid) throws IDMStoreException {
        try {
            return domainStore.getElement(domainid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Domain deleteDomain(String domainid) throws IDMStoreException {
        try {
            return domainStore.deleteElement(domainid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Domain updateDomain(Domain domain) throws IDMStoreException {
        try {
            return domainStore.putElement(domain);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Domains getDomains() throws IDMStoreException {
        try {
            return domainStore.getCollection();
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Role writeRole(Role role) throws IDMStoreException {
        try{
            return roleStore.createElement(role);
        }catch(StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Role readRole(String roleid) throws IDMStoreException {
        try{
            return roleStore.getElement(roleid);
        }catch(StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Role deleteRole(String roleid) throws IDMStoreException {
        try{
            return roleStore.deleteElement(roleid);
        }catch(StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Role updateRole(Role role) throws IDMStoreException {
        try{
            return roleStore.putElement(role);
        }catch(StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Roles getRoles() throws IDMStoreException {
        try {
            return roleStore.getCollection();
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public User writeUser(User user) throws IDMStoreException {
        try {
            user.setSalt(SHA256Calculator.generateSALT());
            user.setPassword(SHA256Calculator.getSHA256(user.getPassword(),user.getSalt()));
            return userStore.createElement(user);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public User readUser(String userid) throws IDMStoreException {
        try {
            return userStore.getElement(userid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public User deleteUser(String userid) throws IDMStoreException {
        try {
            return userStore.deleteElement(userid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public User updateUser(User user) throws IDMStoreException {
        try {
            if(user.getPassword()!=null) {
                user.setSalt(SHA256Calculator.generateSALT());
                user.setPassword(SHA256Calculator.getSHA256(user.getPassword(), user.getSalt()));
            }
            return userStore.putElement(user);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Users getUsers(String username, String domain) throws IDMStoreException {
        try {
            String userID = IDMStoreUtil.createUserid(username,domain);
            return userStore.getCollection(userID);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Users getUsers() throws IDMStoreException {
        try {
            return userStore.getCollection();
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grant writeGrant(Grant grant) throws IDMStoreException {
        try {
            return grantStore.createElement(grant);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grant readGrant(String grantid) throws IDMStoreException {
        try {
            return grantStore.getElement(grantid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grant deleteGrant(String grantid) throws IDMStoreException {
        try {
            return grantStore.deleteElement(grantid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grants getGrants(String domainid, String userid) throws IDMStoreException {
        try {
            Grants grants = grantStore.getCollection();
            List<Grant> list = new ArrayList<>();
            for(Grant g:grants.getGrants()){
                if(g.getDomainid().equals(domainid) && g.getUserid().equals(userid)){
                    list.add(g);
                }
            }
            grants.setGrants(list);
            return grants;
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grants getGrants(String userid) throws IDMStoreException {
        try {
            Grants grants = grantStore.getCollection();
            List<Grant> list = new ArrayList<>();
            for(Grant g:grants.getGrants()){
                if(g.getUserid().equals(userid)){
                    list.add(g);
                }
            }
            grants.setGrants(list);
            return grants;
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grant readGrant(String domainid, String userid, String roleid) throws IDMStoreException {
        return readGrant(IDMStoreUtil.createGrantid(userid, domainid, roleid));
    }

    public void dbClean() throws StoreException {
        domainStore.dbClean();
        roleStore.dbClean();
        userStore.dbClean();
        grantStore.dbClean();
    }
}
