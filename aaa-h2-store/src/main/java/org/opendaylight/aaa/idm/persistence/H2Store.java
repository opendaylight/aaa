package org.opendaylight.aaa.idm.persistence;

import java.sql.Connection;
import java.sql.DriverManager;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.idm.config.IdmLightConfig;

public class H2Store implements IIDMStore{
    private static IdmLightConfig config = new IdmLightConfig();
    private DomainStore domainStore = new DomainStore();
    private UserStore userStore = new UserStore();
    private RoleStore roleStore = new RoleStore();
    private GrantStore grantStore = new GrantStore();

    public H2Store() {
        StoreBuilder storeBuilder = new StoreBuilder();
        if (!storeBuilder.exists()) {
          storeBuilder.init();
        }
    }

    public static Connection getConnection(Connection existingConnection) throws StoreException {
        Connection connection = existingConnection;
        try {
            if (existingConnection == null || existingConnection.isClosed()) {
                new org.h2.Driver();
                connection = DriverManager.getConnection(config.getDbPath(),
                        config.getDbUser(), config.getDbPwd());
            }
        } catch (Exception e) {
            throw new StoreException("Cannot connect to database server " + e);
        }

        return connection;
    }

    public static IdmLightConfig getConfig(){
        return config;
    }

    @Override
    public Domain writeDomain(Domain domain) throws IDMStoreException {
        try {
            return domainStore.createDomain(domain);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Domain readDomain(String domainid) throws IDMStoreException {
        try {
            return domainStore.getDomain(domainid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Domain deleteDomain(String domainid) throws IDMStoreException {
        try {
            return domainStore.deleteDomain(domainid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Domain updateDomain(Domain domain) throws IDMStoreException {
        try {
            return domainStore.putDomain(domain);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Domains getDomains() throws IDMStoreException {
        try {
            return domainStore.getDomains();
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Role writeRole(Role role) throws IDMStoreException {
        try{
            return roleStore.createRole(role);
        }catch(StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Role readRole(String roleid) throws IDMStoreException {
        try{
            return roleStore.getRole(roleid);
        }catch(StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Role deleteRole(String roleid) throws IDMStoreException {
        try{
            return roleStore.deleteRole(roleid);
        }catch(StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Role updateRole(Role role) throws IDMStoreException {
        try{
            return roleStore.putRole(role);
        }catch(StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Roles getRoles() throws IDMStoreException {
        try {
            return roleStore.getRoles();
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public User writeUser(User user) throws IDMStoreException {
        try {
            return userStore.createUser(user);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public User readUser(String userid) throws IDMStoreException {
        try {
            return userStore.getUser(userid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public User deleteUser(String userid) throws IDMStoreException {
        try {
            return userStore.deleteUser(userid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public User updateUser(User user) throws IDMStoreException {
        try {
            return userStore.putUser(user);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Users getUsers(String username, String domain) throws IDMStoreException {
        try {
            return userStore.getUsers(username, domain);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Users getUsers() throws IDMStoreException {
        try {
            return userStore.getUsers();
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grant writeGrant(Grant grant) throws IDMStoreException {
        try {
            return grantStore.createGrant(grant);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grant readGrant(String grantid) throws IDMStoreException {
        try {
            return grantStore.getGrant(grantid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grant deleteGrant(String grantid) throws IDMStoreException {
        try {
            return grantStore.deleteGrant(grantid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grants getGrants(String domainid, String userid) throws IDMStoreException {
        try {
            return grantStore.getGrants(domainid, userid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grants getGrants(String userid) throws IDMStoreException {
        try {
            return grantStore.getGrants(userid);
        } catch (StoreException e) {
            throw new IDMStoreException(e.getMessage());
        }
    }

    @Override
    public Grant readGrant(String domainid, String userid, String roleid) throws IDMStoreException {
        return readGrant(IDMStoreUtil.createGrantid(userid, domainid, roleid));
    }
}
