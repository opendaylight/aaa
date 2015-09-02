package org.opendaylight.aaa.authn.mdsal.store;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.idm.model.Domain;
import org.opendaylight.aaa.idm.model.Grant;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.User;

public class IDMStore implements IIDMStore{
    private final IDMMDSALStore mdsalStore;

    public IDMStore(IDMMDSALStore mdsalStore){
        this.mdsalStore = mdsalStore;
    }

    @Override
    public Domain writeDomain(Domain domain) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMDomain(mdsalStore.writeDomain(IDMObject2MDSAL.toMDSALDomain(domain)));
    }

    @Override
    public Domain readDomain(String domainid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMDomain(mdsalStore.readDomain(domainid));
    }

    @Override
    public Domain deleteDomain(String domainid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMDomain(mdsalStore.deleteDomain(domainid));
    }

    @Override
    public Domain updateDomain(Domain domain) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMDomain(mdsalStore.updateDomain(IDMObject2MDSAL.toMDSALDomain(domain)));
    }

    @Override
    public Role writeRole(Role role) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMRole(mdsalStore.writeRole(IDMObject2MDSAL.toMDSALRole(role)));
    }

    @Override
    public Role readRole(String roleid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMRole(mdsalStore.readRole(roleid));
    }

    @Override
    public Role deleteRole(String roleid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMRole(mdsalStore.deleteRole(roleid));
    }

    @Override
    public Role updateRole(Role role) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMRole(mdsalStore.writeRole(IDMObject2MDSAL.toMDSALRole(role)));
    }

    @Override
    public User writeUser(User user) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMUser(mdsalStore.writeUser(IDMObject2MDSAL.toMDSALUser(user)));
    }

    @Override
    public User readUser(String userid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMUser(mdsalStore.readUser(userid));
    }

    @Override
    public User deleteUser(String userid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMUser(mdsalStore.deleteUser(userid));
    }

    @Override
    public User updateUser(User user) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMUser(mdsalStore.writeUser(IDMObject2MDSAL.toMDSALUser(user)));
    }

    @Override
    public Grant writeGrant(Grant grant) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMGrant(mdsalStore.writeGrant(IDMObject2MDSAL.toMDSALGrant(grant)));
    }

    @Override
    public Grant readGrant(String grantid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMGrant(mdsalStore.readGrant(grantid));
    }

    @Override
    public Grant deleteGrant(String grantid) throws IDMStoreException {
        return IDMObject2MDSAL.toIDMGrant(mdsalStore.readGrant(grantid));
    }
}
