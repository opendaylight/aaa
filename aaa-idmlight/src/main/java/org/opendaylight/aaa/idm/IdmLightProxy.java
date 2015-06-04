/* Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm;

import static org.opendaylight.aaa.idm.persistence.StoreBuilder.DEFAULT_DOMAIN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.idm.model.Domain;
import org.opendaylight.aaa.idm.model.Domains;
import org.opendaylight.aaa.idm.model.Grant;
import org.opendaylight.aaa.idm.model.Grants;
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.User;
import org.opendaylight.aaa.idm.model.Users;
import org.opendaylight.aaa.idm.persistence.DomainStore;
import org.opendaylight.aaa.idm.persistence.GrantStore;
import org.opendaylight.aaa.idm.persistence.RoleStore;
import org.opendaylight.aaa.idm.persistence.StoreException;
import org.opendaylight.aaa.idm.persistence.UserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An OSGi proxy for the IdmLight server.
 *
 */
public class IdmLightProxy implements CredentialAuth<PasswordCredentials>,
        IdMService {

    private static Logger logger = LoggerFactory.getLogger(IdmLightProxy.class);
    private static UserStore userStore = new UserStore();
    private static GrantStore grantStore = new GrantStore();
    private static DomainStore domainStore = new DomainStore();
    private static RoleStore roleStore = new RoleStore();

    // Simple map of claim cache by domain names
    private static Map<String, Map<PasswordCredentials, Claim>> claimCache = new ConcurrentHashMap<>();
    static {
        claimCache.put(DEFAULT_DOMAIN, new ConcurrentHashMap<PasswordCredentials, Claim>());
    }

    @Override
    public Claim authenticate(PasswordCredentials creds, String domain) {
        String domainName = (domain == null) ? DEFAULT_DOMAIN : domain;
        // FIXME: Add cache invalidation
        Map<PasswordCredentials, Claim> cache = claimCache.get(domainName);
        if (cache == null) {
            cache = new ConcurrentHashMap<PasswordCredentials, Claim>();
            claimCache.put(domainName, cache);
        }
        Claim claim = cache.get(creds);
        if (claim == null) {
            synchronized (claimCache) {
                claim = cache.get(creds);
                if (claim == null) {
                    claim = dbAuthenticate(creds, domainName);
                    if (claim != null) {
                        cache.put(creds, claim);
                    }
                }
            }
        }
        return claim;
    }

    public static synchronized void clearClaimCache() {
        for (Map<PasswordCredentials, Claim> cache : claimCache.values()) {
            cache.clear();
        }
    }

    private static Claim dbAuthenticate(PasswordCredentials creds, String domainName) {
        Domain domain=null;
        User user=null;
        // check to see domain exists
        // TODO: ensure domain names are unique change to 'getDomain'
        debug("get domain");
        try {
           Domains domains = domainStore.getDomains(domainName);
           List<Domain> domainList = domains.getDomains();
           if (domainList.size()==0) {
              throw new AuthenticationException("Domain :" + domainName + " does not exist");
           }
           domain = domainList.get(0);
        }
        catch (StoreException se) {
           throw new AuthenticationException("idm data store exception :" + se.toString() + se);
        }

        // check to see user exists and passes cred check
        try {
           debug("check user / pwd");
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
           debug("get grants");
           List<String> roles = new ArrayList<String>();
           Grants grants = grantStore.getGrants(domain.getDomainid(),user.getUserid());
           List<Grant> grantList = grants.getGrants();
           for (int z=0;z<grantList.size();z++) {
              Grant grant = grantList.get(z);
              Role role = roleStore.getRole(grant.getRoleid());
              roles.add(role.getName());
           }

           // build up the claim
           debug("build a claim");
           ClaimBuilder claim = new ClaimBuilder();
           claim.setUserId(user.getUserid().toString());
           claim.setUser(creds.username());
           claim.setDomain(domainName);
           for (int z=0;z<roles.size();z++) {
              claim.addRole(roles.get(z));
           }
           return claim.build();
        }
        catch (StoreException se) {
           throw new AuthenticationException("idm data store exception :" + se.toString() + se);
        }
    }

    @Override
    public String getUserId(String userName) {
        debug("getUserid for userName:" + userName);
        try {
           Users users = userStore.getUsers(userName);
           List<User> userList = users.getUsers();
           if (userList.size()==0) {
              return null;
           }
           User user = userList.get(0);
           return user.getUserid().toString();
        }
        catch (StoreException se) {
           logger.warn("error getting user " , se.toString(), se);
           return null;
        }
    }

    @Override
    public List<String> listDomains(String userId) {
        debug("list Domains for userId:" + userId);
        List<String> domains = new ArrayList<String>();
        int uid=0;
        try {
           uid = Integer.parseInt(userId);
        }
        catch (NumberFormatException nfe) {
           logger.warn("not a valid userid:" ,userId, nfe);
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
           logger.warn("error getting domains " , se.toString(), se);
           return domains;
        }

    }

    @Override
    public List<String> listRoles(String userId, String domainName) {
        debug("listRoles");
        List<String> roles = new ArrayList<String>();

        try {
           // find domain name for specied domain name
           Domains domains = domainStore.getDomains(domainName);
           List<Domain> domainList = domains.getDomains();
           if (domainList.size()==0) {
              debug("DomainName: " + domainName + " Not found!");
              return roles;
           }
           int did = domainList.get(0).getDomainid();

           // validate userId
           int uid=0;
           try {
              uid = Integer.parseInt(userId);
           }
           catch (NumberFormatException nfe) {
              logger.warn("not a valid userid:" ,userId, nfe);
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
           logger.warn("error getting roles " , se.toString(), se);
           return roles;
        }
    }

    private static final void debug(String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg);
        }
    }
}

