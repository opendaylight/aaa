/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.rest.test;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.Roles;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;

public class IDMTestStore implements IIDMStore {

    private List<Domain> domains = new ArrayList<Domain>();
    private List<Grant> grants = new ArrayList<Grant>();
    private List<Role> roles = new ArrayList<Role>();
    private List<User> users = new ArrayList<User>();

    public IDMTestStore() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Domain writeDomain(Domain domain) throws IDMStoreException {
        domain.setDomainid(String.valueOf(domains.size()));
        domains.add(domain);
        return domain;
    }

    @Override
    public Domain readDomain(String domainid) throws IDMStoreException {
        for(Domain dom : domains)  {
            if (dom.getDomainid().equals(domainid)) {
                return dom;
            }
        }
        return null;
    }

    @Override
    public Domain deleteDomain(String domainid) throws IDMStoreException {
        for(Domain dom : domains)  {
            if (dom.getDomainid().equals(domainid)) {
                domains.remove(dom);
                return dom;
            }
        }
        return null;
    }

    @Override
    public Domain updateDomain(Domain domain) throws IDMStoreException {
        for(Domain dom : domains)  {
            if (dom.getDomainid().equals(domain.getDomainid())) {
                domains.remove(dom);
                domains.add(domain);
                return domain;
            }
        }
        return null;
    }

    @Override
    public Domains getDomains() throws IDMStoreException {
        Domains doms =  new Domains();
        doms.setDomains(domains);
        return doms;
    }

    @Override
    public Role writeRole(Role role) throws IDMStoreException {
        role.setRoleid(String.valueOf(roles.size()));
        roles.add(role);
        return role;
    }

    @Override
    public Role readRole(String roleid) throws IDMStoreException {
        for (Role role : roles) {
            if (role.getRoleid().equals(roleid)) {
                return role;
            }
        }
        return null;
    }

    @Override
    public Role deleteRole(String roleid) throws IDMStoreException {
        for (Role role : roles) {
            if (role.getRoleid().equals(roleid)) {
                roles.remove(role);
                return role;
            }
        }
        return null;
    }

    @Override
    public Role updateRole(Role role) throws IDMStoreException {
        for (Role inRole : roles) {
            if (inRole.getRoleid().equals(role.getRoleid())) {
                roles.remove(inRole);
                roles.add(role);
                return role;
            }
        }
        return null;
    }

    @Override
    public Roles getRoles() throws IDMStoreException {
        Roles rols = new Roles();
        rols.setRoles(roles);
        return rols;
    }

    @Override
    public User writeUser(User user) throws IDMStoreException {
        user.setUserid(String.valueOf(users.size()));
        users.add(user);
        return user;
    }

    @Override
    public User readUser(String userid) throws IDMStoreException {
        for(User usr : users) {
            if (usr.getUserid().equals(userid)) {
                return usr;
            }
        }
        return null;
    }

    @Override
    public User deleteUser(String userid) throws IDMStoreException {
        for(User usr : users) {
            if (usr.getUserid().equals(userid)) {
                users.remove(usr);
                return usr;
            }
        }
        return null;
    }

    @Override
    public User updateUser(User user) throws IDMStoreException {
        for(User usr : users) {
            if (usr.getUserid().equals(user.getUserid())) {
                users.remove(usr);
                users.add(user);
                return usr;
            }
        }
        return null;
    }

    @Override
    public Users getUsers() throws IDMStoreException {
        Users usrs = new Users();
        usrs.setUsers(users);
        return usrs;
    }

    @Override
    public Users getUsers(String username, String domainId) throws IDMStoreException {
        Users usrs = new Users();
        User user = null;
        Domain domain = null;
        for(User usr : users) {
            if (usr.getName().equals(username)) {
                user = usr;
                break;
            }
        }
        for(Domain dom : domains) {
            if (dom.getDomainid().equals(domainId)) {
                domain = dom;
                break;
            }
        }
        if (user == null || domain == null)
            return usrs;
        for (Grant grant : grants) {
            if (grant.getUserid().equals(user.getUserid()) && grant.getDomainid().equals(domain.getDomainid())) {
                List<User> usrList = new ArrayList<User>();
                usrList.add(user);
                usrs.setUsers(usrList);
                break;
            }
        }
        return usrs;
    }

    @Override
    public Grant writeGrant(Grant grant) throws IDMStoreException {
        grant.setGrantid(String.valueOf(grants.size()));
        grants.add(grant);
        return grant;
    }

    @Override
    public Grant readGrant(String grantid) throws IDMStoreException {
        for (Grant grant : grants) {
            if (grant.getGrantid().equals(grantid)) {
                return grant;
            }
        }
        return null;
    }

    @Override
    public Grant deleteGrant(String grantid) throws IDMStoreException {
        for (Grant grant : grants) {
            if (grant.getGrantid().equals(grantid)) {
                grants.remove(grant);
                return grant;
            }
        }
        return null;
    }

    @Override
    public Grants getGrants(String domainid, String userid) throws IDMStoreException {
        Grants usrGrants = new Grants();
        List<Grant> usrGrant = new ArrayList<Grant>();
        for (Grant grant : grants) {
            if (grant.getUserid().equals(userid) && grant.getDomainid().equals(domainid)) {
                usrGrant.add(grant);
            }
        }
        usrGrants.setGrants(usrGrant);
        return usrGrants;
    }

    @Override
    public Grants getGrants(String userid) throws IDMStoreException {
        Grants usrGrants = new Grants();
        List<Grant> usrGrant = new ArrayList<Grant>();
        for (Grant grant : grants) {
            if (grant.getUserid().equals(userid)) {
                usrGrant.add(grant);
            }
        }
        usrGrants.setGrants(usrGrant);
        return usrGrants;
    }

    @Override
    public Grant readGrant(String domainid, String userid, String roleid) throws IDMStoreException {
        for (Grant grant : grants) {
            if (grant.getDomainid().equals(domainid) && grant.getUserid().equals(userid) && grant.getRoleid().equals(roleid)) {
                return grant;
            }
        }
        return null;
    }

    @Override
    public boolean isMainNodeInCluster() {
        return true;
    }
}
