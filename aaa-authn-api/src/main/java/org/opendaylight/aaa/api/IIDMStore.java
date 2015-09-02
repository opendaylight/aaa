package org.opendaylight.aaa.api;

import org.opendaylight.aaa.idm.model.Domain;
import org.opendaylight.aaa.idm.model.Grant;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.User;

public interface IIDMStore {
    //Domain methods
    public Domain writeDomain(Domain domain) throws IDMStoreException;
    public Domain readDomain(String domainid) throws IDMStoreException;
    public Domain deleteDomain(String domainid) throws IDMStoreException;
    public Domain updateDomain(Domain domain) throws IDMStoreException;
    //Role methods
    public Role writeRole(Role role) throws IDMStoreException;
    public Role readRole(String roleid) throws IDMStoreException;
    public Role deleteRole(String roleid) throws IDMStoreException;
    public Role updateRole(Role role) throws IDMStoreException;
    //User methods
    public User writeUser(User user) throws IDMStoreException;
    public User readUser(String userid) throws IDMStoreException;
    public User deleteUser(String userid) throws IDMStoreException;
    public User updateUser(User user) throws IDMStoreException;
    //Grant methods
    public Grant writeGrant(Grant grant) throws IDMStoreException;
    public Grant readGrant(String grantid) throws IDMStoreException;
    public Grant deleteGrant(String grantid) throws IDMStoreException;
}
