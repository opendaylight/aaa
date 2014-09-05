/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentials;

import java.util.ArrayList;
import org.opendaylight.aaa.idm.model.Users;
import org.opendaylight.aaa.idm.model.User;
import org.opendaylight.aaa.idm.model.Grants;
import org.opendaylight.aaa.idm.model.Grant;
import org.opendaylight.aaa.idm.model.Roles;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.Domains;
import org.opendaylight.aaa.idm.model.Domain;
import org.opendaylight.aaa.idm.model.IDMError;

import org.opendaylight.aaa.idm.persistence.UserStore;
import org.opendaylight.aaa.idm.persistence.GrantStore;
import org.opendaylight.aaa.idm.persistence.DomainStore;
import org.opendaylight.aaa.idm.persistence.RoleStore;
import org.opendaylight.aaa.idm.persistence.StoreException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An OSGi proxy for the IdmLight server.
 *
 * @author liemmn
 */
public class IdmLightProxy implements CredentialAuth<PasswordCredentials>,
        IdMService {
    
    private static Logger logger = LoggerFactory.getLogger(IdmLightProxy.class);
    private static UserStore userStore = new UserStore();
    private static GrantStore grantStore = new GrantStore();
    private static DomainStore domainStore = new DomainStore();
    private static RoleStore roleStore = new RoleStore();

    @Override
    public Claim authenticate(PasswordCredentials creds, String domainName) {
        Domain domain=null;
        User user=null;

        // check to see domain exists
        // TODO: ensure domain names are unique change to 'getDomain'
        try {
           Domains domains = domainStore.getDomains(domainName);
           List<Domain> domainList = domains.getDomains();
           if (domainList.size()==0) {
              throw new AuthenticationException("Domain :" + domainName + " does not exist"); 
           }
           domain = domainList.get(0);
        }
        catch (StoreException se) {
           throw new AuthenticationException("idm data store exception :" + se.toString()); 
        }

        // check to see user exists and passes cred check
        try {
           Users users = userStore.getUsers(creds.username());
           List<User> userList = users.getUsers();
           if (userList.size()==0) {
              throw new AuthenticationException("User :" + creds.username() + " does not exist");
           }
           user = userList.get(0);
           if (!creds.password().equalsIgnoreCase(user.getPassword())) {
              throw new AuthenticationException("UserName / Password not found");
           }

           // get all grants & roles for this domain and user
           List<String> roles = new ArrayList<String>();
           Grants grants = grantStore.getGrants(domain.getDomainid(),user.getUserid());
           List<Grant> grantList = grants.getGrants();
           for (int z=0;z<grantList.size();z++) {
              Grant grant = grantList.get(z);
              Role role = roleStore.getRole(grant.getRoleid());
              roles.add(role.getName());
           }

           // build up the claim
           ClaimBuilder claim = new ClaimBuilder();
           claim.setUserId(user.getUserid().toString());
           claim.setUser(creds.username());
           claim.setDomain(domainName);
           for (int z=0;z<roles.size();z++)
              claim.addRole(roles.get(z));
           return claim.build();
        }
        catch (StoreException se) {
           throw new AuthenticationException("idm data store exception :" + se.toString());
        }

    }

    @Override
    public String getUserId(String userName) {
        logger.info("getUserid for userName:" + userName);
        try { 
           Users users = userStore.getUsers(userName); 
           List<User> userList = users.getUsers();
           if (userList.size()==0)
              return null;
           User user = userList.get(0);
           return user.getUserid().toString();
        } 
        catch (StoreException se) { 
           logger.warn("error getting user " + se.toString());        
           return null;
        }
    }

    @Override
    public List<String> listDomains(String userId) {
        logger.info("list Domains for userId:" + userId);
        List<String> domains = new ArrayList<String>(); 
        long uid=0;
        try {
           uid = Long.parseLong(userId);
        }
        catch (NumberFormatException nfe) {
           logger.warn("not a valid userid:" + userId);
           return domains;
        } 
        try {
           Grants grants = grantStore.getGrants(uid);
           List<Grant> grantList = grants.getGrants();
           for (int z=0;z<grantList.size();z++) {
              Grant grant = grantList.get(z);
              Domain domain = domainStore.getDomain(grant.getDomainid());
              domains.add(domain.getName());
           }
           return domains;
        }
        catch (StoreException se) {
           logger.warn("error getting domains " + se.toString());
           return domains;
        }

    }

    @Override
    public List<String> listRoles(String userId, String domainName) {
        List<String> roles = new ArrayList<String>();

        try {
           // find domain name for specied domain name
           Domains domains = domainStore.getDomains(domainName);
           List<Domain> domainList = domains.getDomains();
           if (domainList.size()==0) {
              logger.info("DomainName: " + domainName + " Not found!");
              return roles;
           }
           long did = domainList.get(0).getDomainid();

           // validate userId
           long uid=0;
           try {
              uid = Long.parseLong(userId);
           }
           catch (NumberFormatException nfe) {
              logger.warn("not a valid userid:" + userId);
              return roles;
           }
 
           // find all grants for uid and did
           Grants grants = grantStore.getGrants(did,uid);
           List<Grant> grantList = grants.getGrants();
           for (int z=0;z<grantList.size();z++) {
              Grant grant = grantList.get(z);
              Role role = roleStore.getRole(grant.getRoleid());
              roles.add(role.getName());
           }

           return roles; 
        }
        catch (StoreException se) {
           logger.warn("error getting roles " + se.toString());
           return roles;
        }
         
    }
}
