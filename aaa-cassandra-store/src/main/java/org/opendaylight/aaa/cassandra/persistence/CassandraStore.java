package org.opendaylight.aaa.cassandra.persistence;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.*;
import org.opendaylight.aaa.cassandra.config.IdmLightConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CassandraStore implements IIDMStore{
    private static final String CONFIG_FILE = "./etc/aaa-cassandra.properties";
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraStore.class);
    private static IdmLightConfig config = new IdmLightConfig();
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
            LOGGER.error("Failed to instantiate stores",e);
        }
    }

    public DomainStore getDomainStore() {
        return domainStore;
    }

    public UserStore getUserStore() {
        return userStore;
    }

    public RoleStore getRoleStore() {
        return roleStore;
    }

    public GrantStore getGrantStore() {
        return grantStore;
    }

    private Map<String,String> loadConfig() {
        HashMap<String, String> result = new HashMap<String,String>();
        try {
            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(new FileInputStream(CONFIG_FILE)));
            String line = in.readLine();
            while (line != null) {
                int index = line.indexOf("=");
                String key = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                result.put(key, value);
                line = in.readLine();
            }
            in.close();
        }catch(IOException e){
            result.put("host","127.0.0.1");
            result.put("main-node","true");
            result.put("replication_factor","3");
        }
        return result;
    }

    public Session getSession() throws IOException {
        if (session == null) {
            synchronized (this) {
                Map<String,String> config = loadConfig();
                this.host = config.get("host");
                this.isMainNode = Boolean.parseBoolean(config.get("main-node"));
                this.replication_factor = Integer.parseInt(config.get("replication_factor"));
                LOGGER.info("Trying to work with " + this.host+ ", Which main node is set to=" + this.isMainNode);
                cluster = Cluster.builder().addContactPoint(host).build();

                // Try 5 times to connect to cassandra with a 5 seconds delay
                // between each try
                for (int index = 0; index < 5; index++) {
                    try {
                        session = cluster.connect("aaa");
                        return session;
                    } catch (InvalidQueryException err) {
                        try {
                            LOGGER.info("Failed to get aaa keyspace...");
                            if (this.isMainNode) {
                                LOGGER.info("This is the main node, trying to create keyspace and tables...");
                                session = cluster.connect();
                                session.execute("CREATE KEYSPACE aaa WITH replication "
                                        + "= {'class':'SimpleStrategy', 'replication_factor':"+replication_factor+"};");
                                session = cluster.connect("aaa");
                                return session;
                            }
                        } catch (Exception err2) {
                            LOGGER.error("Failed to create keyspace & tables, will retry in 5 seconds...",err2);
                        }
                    }
                    LOGGER.info("Sleeping for 5 seconds...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        LOGGER.error("Interrupted",e);
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

    public static IdmLightConfig getConfig(){
        return config;
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
}
