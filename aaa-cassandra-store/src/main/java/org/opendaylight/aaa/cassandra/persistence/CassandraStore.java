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
@Deprecated
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
        return domainStore.createElement(domain);
    }

    @Override
    public Domain readDomain(String domainid) throws IDMStoreException {
        return domainStore.getElement(domainid);
    }

    @Override
    public Domain deleteDomain(String domainid) throws IDMStoreException {
        return domainStore.deleteElement(domainid);
    }

    @Override
    public Domain updateDomain(Domain domain) throws IDMStoreException {
        return domainStore.putElement(domain);
    }

    @Override
    public Domains getDomains() throws IDMStoreException {
        return domainStore.getCollection();
    }

    @Override
    public Role writeRole(Role role) throws IDMStoreException {
        return roleStore.createElement(role);
    }

    @Override
    public Role readRole(String roleid) throws IDMStoreException {
        return roleStore.getElement(roleid);
    }

    @Override
    public Role deleteRole(String roleid) throws IDMStoreException {
        return roleStore.deleteElement(roleid);
    }

    @Override
    public Role updateRole(Role role) throws IDMStoreException {
        return roleStore.putElement(role);
    }

    @Override
    public Roles getRoles() throws IDMStoreException {
        return roleStore.getCollection();
    }

    @Override
    public User writeUser(User user) throws IDMStoreException {
        user.setSalt(SHA256Calculator.generateSALT());
        user.setPassword(SHA256Calculator.getSHA256(user.getPassword(),user.getSalt()));
        return userStore.createElement(user);
    }

    @Override
    public User readUser(String userid) throws IDMStoreException {
        return userStore.getElement(userid);
    }

    @Override
    public User deleteUser(String userid) throws IDMStoreException {
        return userStore.deleteElement(userid);
    }

    @Override
    public User updateUser(User user) throws IDMStoreException {
        if(user.getPassword()!=null) {
            user.setSalt(SHA256Calculator.generateSALT());
            user.setPassword(SHA256Calculator.getSHA256(user.getPassword(), user.getSalt()));
        }
        return userStore.putElement(user);
    }

    @Override
    public Users getUsers(String username, String domain) throws IDMStoreException {
        String userID = IDMStoreUtil.createUserid(username,domain);
        return userStore.getCollection(userID);
    }

    @Override
    public Users getUsers() throws IDMStoreException {
        return userStore.getCollection();
    }

    @Override
    public Grant writeGrant(Grant grant) throws IDMStoreException {
        return grantStore.createElement(grant);
    }

    @Override
    public Grant readGrant(String grantid) throws IDMStoreException {
        return grantStore.getElement(grantid);
    }

    @Override
    public Grant deleteGrant(String grantid) throws IDMStoreException {
        return grantStore.deleteElement(grantid);
    }

    @Override
    public Grants getGrants(String domainid, String userid) throws IDMStoreException {
        Grants grants = grantStore.getCollection();
        List<Grant> list = new ArrayList<>();
        for(Grant g:grants.getGrants()){
            if(g.getDomainid().equals(domainid) && g.getUserid().equals(userid)){
                list.add(g);
            }
        }
        grants.setGrants(list);
        return grants;
    }

    @Override
    public Grants getGrants(String userid) throws IDMStoreException {
        Grants grants = grantStore.getCollection();
        List<Grant> list = new ArrayList<>();
        for(Grant g:grants.getGrants()){
            if(g.getUserid().equals(userid)){
                list.add(g);
            }
        }
        grants.setGrants(list);
        return grants;
    }

    @Override
    public Grant readGrant(String domainid, String userid, String roleid) throws IDMStoreException {
        return readGrant(IDMStoreUtil.createGrantid(userid, domainid, roleid));
    }

    public void dbClean() throws IDMStoreException {
        domainStore.dbClean();
        roleStore.dbClean();
        userStore.dbClean();
        grantStore.dbClean();
    }

    @Override
    public boolean isMainNodeInCluster() {
        return CassandraConfig.getInstance().isMainNode();
    }
}
