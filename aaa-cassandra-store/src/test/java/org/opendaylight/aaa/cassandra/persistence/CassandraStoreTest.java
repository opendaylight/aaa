/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cassandra.persistence;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ExecutionInfo;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.Token;
import com.datastax.driver.core.TupleValue;
import com.datastax.driver.core.UDTValue;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;

/**
 * Created by saichler@gmail.com on 11/29/15.
 */
public class CassandraStoreTest {
    private CassandraStore store = null;
    private static final boolean USE_REAL_CASSANDRA = false;

    private Cluster cluster = mock(Cluster.class);
    private Metadata metadata = mock(Metadata.class);
    private KeyspaceMetadata keyspaceMetadata = mock(KeyspaceMetadata.class);
    private TableMetadata tableMetadata = mock(TableMetadata.class);
    private Session session = mock(Session.class);
    private MockedResultSet rs = new MockedResultSet();

    @BeforeClass
    public static final void beforeClass() throws StoreException {
        if(USE_REAL_CASSANDRA) {
            CassandraStore store = new CassandraStore();
            store.dbClean();
        }
    }

    @AfterClass
    public static final void afterClass() throws StoreException {
        if(USE_REAL_CASSANDRA) {
            CassandraStore store = new CassandraStore();
            store.dbClean();
        }
    }

    @Before
    public void setup(){
        if(store==null){
            if(USE_REAL_CASSANDRA){
                store = new CassandraStore();
            }else{
                store = new CassandraStore(cluster,session);
                setupMocks();
            }
        }
    }
    private void setupMocks(){
        when(cluster.getMetadata()).thenReturn(metadata);
        when(metadata.getKeyspace("aaa")).thenReturn(keyspaceMetadata);
        when(keyspaceMetadata.getTable("Domain")).thenReturn(tableMetadata);
        when(keyspaceMetadata.getTable("Role")).thenReturn(tableMetadata);
        when(keyspaceMetadata.getTable("User")).thenReturn(tableMetadata);
        when(keyspaceMetadata.getTable("GGrant")).thenReturn(tableMetadata);
        when(session.execute("SELECT * FROM GGrant where Grantid = 'test@sdn@test@sdn@sdn'")).thenReturn(rs);
        when(session.execute("SELECT * FROM Role where Roleid = 'test@sdn'")).thenReturn(rs);
        when(session.execute("SELECT * FROM User where Userid = 'test@sdn'")).thenReturn(rs);
        when(session.execute("SELECT * FROM Domain where Domainid = 'sdn'")).thenReturn(rs);
        when(session.execute("SELECT * FROM GGrant")).thenReturn(rs);
        when(session.execute("SELECT * FROM Role")).thenReturn(rs);
        when(session.execute("SELECT * FROM Domain")).thenReturn(rs);
        when(session.execute("SELECT * FROM User")).thenReturn(rs);
        when(session.execute("SELECT * FROM GGrant where Grantid = 'SomeValue'")).thenReturn(rs);
        when(session.execute("SELECT * FROM Role where Roleid = 'SomeValue'")).thenReturn(rs);
        when(session.execute("SELECT * FROM User where Userid = 'SomeValue'")).thenReturn(rs);
        when(session.execute("SELECT * FROM Domain where Domainid = 'SomeValue'")).thenReturn(rs);
    }
    @Test
    public void testDomain() throws Exception{
        rs.deleted = false;
        Domain domain = new Domain();
        domain.setName("sdn");
        domain.setEnabled(true);
        domain.setDescription("SDN Domain");
        Domain d = store.writeDomain(domain);
        Assert.assertNotNull(d);
        d = store.readDomain("sdn");
        Assert.assertNotNull(d);
        Domains domains = store.getDomains();
        Assert.assertNotNull(domains);
        Assert.assertTrue(domains.getDomains().size()==1);
        Assert.assertNotNull(domains.getDomains().get(0).getDescription());
        domain.setDescription("HelloWorld");
        d = store.updateDomain(domain);
        Assert.assertNotNull(d);
        Assert.assertEquals("HelloWorld",d.getDescription());
        d = store.deleteDomain("sdn");
        Assert.assertNotNull(d);
        rs.deleted = true;
        d = store.deleteDomain("sdn");
        Assert.assertNull(d);
    }

    @Test
    public void testRole() throws Exception {
        rs.deleted = false;
        Role role = new Role();
        role.setDescription("Test Role");
        role.setDomainid("sdn");
        role.setName("test");
        Role r = store.writeRole(role);
        Assert.assertNotNull(r);
        r = store.readRole(r.getRoleid());
        Assert.assertNotNull(r);
        Roles roles = store.getRoles();
        Assert.assertNotNull(roles);
        Assert.assertTrue(roles.getRoles().size()==1);
        Assert.assertNotNull(roles.getRoles().get(0).getDescription());
        r.setDescription("HelloWorld");
        r = store.updateRole(r);
        Assert.assertNotNull(r);
        Assert.assertEquals("HelloWorld",r.getDescription());
        r = store.deleteRole(r.getRoleid());
        Assert.assertNotNull(r);
        rs.deleted = true;
        r = store.deleteRole(r.getRoleid());
        Assert.assertNull(r);
    }

    @Test
    public void testUser() throws Exception {
        rs.deleted = false;
        User user = new User();
        user.setDescription("Test User");
        user.setDomainid("sdn");
        user.setName("test");
        user.setEmail("test@test.com");
        user.setPassword("test");
        User u = store.writeUser(user);
        Assert.assertNotNull(u);
        u = store.readUser(u.getUserid());
        Assert.assertNotNull(u);
        Users roles = store.getUsers();
        Assert.assertNotNull(roles);
        Assert.assertTrue(roles.getUsers().size()==1);
        Assert.assertNotNull(roles.getUsers().get(0).getDescription());
        u.setDescription("HelloWorld");
        u = store.updateUser(u);
        Assert.assertNotNull(u);
        Assert.assertEquals("HelloWorld",u.getDescription());
        u = store.deleteUser(u.getUserid());
        Assert.assertNotNull(u);
        rs.deleted = true;
        u = store.deleteUser(u.getUserid());
        Assert.assertNull(u);
    }

    @Test
    public void testGrant() throws Exception {
        rs.deleted = false;
        Grant grant = new Grant();
        grant.setDomainid("sdn");
        grant.setRoleid("test@sdn");
        grant.setUserid("test@sdn");
        Grant g = store.writeGrant(grant);
        Assert.assertNotNull(g);
        g = store.readGrant(g.getGrantid());
        Assert.assertNotNull(g);
        Grants grants = store.getGrants("test@sdn");
        Assert.assertNotNull(grants);
        Assert.assertTrue(grants.getGrants().size()==1);

        grants = store.getGrants("sdn","test@sdn");
        Assert.assertNotNull(grants);
        Assert.assertTrue(grants.getGrants().size()==1);

        g = store.deleteGrant(g.getGrantid());
        Assert.assertNotNull(g);
        rs.deleted = true;
        g = store.deleteGrant(g.getGrantid());
        Assert.assertNull(g);
    }

    private class MockedResultSet implements ResultSet {
        MockRow row = new MockRow();
        boolean deleted = false;
        @Override
        public ColumnDefinitions getColumnDefinitions() {
            return null;
        }

        @Override
        public boolean isExhausted() {
            return false;
        }

        @Override
        public Row one() {
            return null;
        }

        @Override
        public List<Row> all() {
            List<Row> result = new LinkedList<>();
            if(!deleted) {
                result.add(row);
            }
            return result;
        }

        @Override
        public Iterator<Row> iterator() {
            return null;
        }

        @Override
        public int getAvailableWithoutFetching() {
            return 0;
        }

        @Override
        public boolean isFullyFetched() {
            return false;
        }

        @Override
        public ListenableFuture<Void> fetchMoreResults() {
            return null;
        }

        @Override
        public ExecutionInfo getExecutionInfo() {
            return null;
        }

        @Override
        public List<ExecutionInfo> getAllExecutionInfo() {
            return null;
        }

        @Override
        public boolean wasApplied() {
            return false;
        }
    }

    private class MockRow implements Row {
        @Override
        public ColumnDefinitions getColumnDefinitions() {
            return null;
        }

        @Override
        public boolean isNull(int i) {
            return false;
        }

        @Override
        public boolean isNull(String s) {
            return false;
        }

        @Override
        public boolean getBool(int i) {
            return false;
        }

        @Override
        public boolean getBool(String s) {
            return true;
        }

        @Override
        public int getInt(int i) {
            return 0;
        }

        @Override
        public int getInt(String s) {
            return 0;
        }

        @Override
        public long getLong(int i) {
            return 0;
        }

        @Override
        public long getLong(String s) {
            return 0;
        }

        @Override
        public Date getDate(int i) {
            return null;
        }

        @Override
        public Date getDate(String s) {
            return null;
        }

        @Override
        public float getFloat(int i) {
            return 0;
        }

        @Override
        public float getFloat(String s) {
            return 0;
        }

        @Override
        public double getDouble(int i) {
            return 0;
        }

        @Override
        public double getDouble(String s) {
            return 0;
        }

        @Override
        public ByteBuffer getBytesUnsafe(int i) {
            return null;
        }

        @Override
        public ByteBuffer getBytesUnsafe(String s) {
            return null;
        }

        @Override
        public ByteBuffer getBytes(int i) {
            return null;
        }

        @Override
        public ByteBuffer getBytes(String s) {
            return null;
        }

        @Override
        public String getString(int i) {
            return null;
        }

        @Override
        public String getString(String s) {
            if(s.equals("Userid")){
                return "test@sdn";
            }else if(s.equals("Domainid")){
                return "sdn";
            }else {
                return "SomeValue";
            }
        }

        @Override
        public BigInteger getVarint(int i) {
            return null;
        }

        @Override
        public BigInteger getVarint(String s) {
            return null;
        }

        @Override
        public BigDecimal getDecimal(int i) {
            return null;
        }

        @Override
        public BigDecimal getDecimal(String s) {
            return null;
        }

        @Override
        public UUID getUUID(int i) {
            return null;
        }

        @Override
        public UUID getUUID(String s) {
            return null;
        }

        @Override
        public InetAddress getInet(int i) {
            return null;
        }

        @Override
        public InetAddress getInet(String s) {
            return null;
        }

        @Override
        public Token getToken(int i) {
            return null;
        }

        @Override
        public Token getToken(String s) {
            return null;
        }

        @Override
        public Token getPartitionKeyToken() {
            return null;
        }

        @Override
        public <T> List<T> getList(int i, Class<T> aClass) {
            return null;
        }

        @Override
        public <T> List<T> getList(String s, Class<T> aClass) {
            return null;
        }

        @Override
        public <T> Set<T> getSet(int i, Class<T> aClass) {
            return null;
        }

        @Override
        public <T> Set<T> getSet(String s, Class<T> aClass) {
            return null;
        }

        @Override
        public <K, V> Map<K, V> getMap(int i, Class<K> aClass, Class<V> aClass1) {
            return null;
        }

        @Override
        public <K, V> Map<K, V> getMap(String s, Class<K> aClass, Class<V> aClass1) {
            return null;
        }

        @Override
        public <T> List<T> getList(int i, TypeToken<T> typeToken) {
            return null;
        }

        @Override
        public <T> Set<T> getSet(int i, TypeToken<T> typeToken) {
            return null;
        }

        @Override
        public <K, V> Map<K, V> getMap(int i, TypeToken<K> typeToken, TypeToken<V> typeToken1) {
            return null;
        }

        @Override
        public UDTValue getUDTValue(int i) {
            return null;
        }

        @Override
        public TupleValue getTupleValue(int i) {
            return null;
        }

        @Override
        public Object getObject(int i) {
            return null;
        }

        @Override
        public <T> List<T> getList(String s, TypeToken<T> typeToken) {
            return null;
        }

        @Override
        public <T> Set<T> getSet(String s, TypeToken<T> typeToken) {
            return null;
        }

        @Override
        public <K, V> Map<K, V> getMap(String s, TypeToken<K> typeToken, TypeToken<V> typeToken1) {
            return null;
        }

        @Override
        public UDTValue getUDTValue(String s) {
            return null;
        }

        @Override
        public TupleValue getTupleValue(String s) {
            return null;
        }

        @Override
        public Object getObject(String s) {
            return null;
        }
    }
}
