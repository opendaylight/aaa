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
import org.opendaylight.aaa.idm.model.Role;
import org.opendaylight.aaa.idm.model.User;
import org.opendaylight.aaa.idm.persistence.JDBCObjectStore;
import org.opendaylight.aaa.idm.persistence.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An OSGi proxy for the IdmLight server.
 *
 */
public class IdmLightProxy implements CredentialAuth<PasswordCredentials>,
        IdMService {

    private static Logger logger = LoggerFactory.getLogger(IdmLightProxy.class);
    private static JDBCObjectStore store = new JDBCObjectStore();

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
        User user = new User();
        // check to see domain exists
        // TODO: ensure domain names are unique change to 'getDomain'
        debug("get domain");
       Domains domains = new Domains();
       if (domains.getDomains().size()==0) {
          throw new AuthenticationException("Domain :" + domainName + " does not exist");
       }
       domain = domains.getDomains().get(0);

        // check to see user exists and passes cred check
        try {
           debug("check user / pwd");
           user.setName(creds.username());
           user = (User)store.getPOJO(user, false);
           if (user == null) {
              throw new AuthenticationException("User :" + creds.username() + " does not exist");
           }

           if (!creds.password().equalsIgnoreCase(user.getPassword())) {
              throw new AuthenticationException("UserName / Password not found");
           }

           // get all grants & roles for this domain and user
           debug("get grants");
           List<String> roles = new ArrayList<String>();
           Grant grantCriteria = new Grant();
           grantCriteria.setUserid(user.getUserid());
           grantCriteria.setDomainid(domain.getDomainid());
           List<Object> grants = store.getPOJOs(grantCriteria, false);
           for (int z=0;z<grants.size();z++) {
              Grant grant = (Grant)grants.get(z);
              Role role = new Role();
              role.setRoleid(grant.getRoleid());
              role = (Role)store.getPOJO(role,false);
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
           User user = new User();
           user.setName(userName);
           user = (User)store.getPOJO(user, false);
           if (user == null) {
              return null;
           }
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
            Grant grantCriteria = new Grant();
            grantCriteria.setUserid(uid);
            List<Object> grants = store.getPOJOs(grantCriteria, false);
           for (int z=0;z<grants.size();z++) {
              Grant grant = (Grant)grants.get(z);
              Domain domain = new Domain();
              domain.setDomainid(grant.getDomainid());
              domain = (Domain)store.getPOJO(domain,false);
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
            Domain domain = new Domain();
            domain.setName(domainName);
            domain = (Domain)store.getPOJO(domain, true);
           if (domain==null) {
              debug("DomainName: " + domainName + " Not found!");
              return roles;
           }
           int did = domain.getDomainid();

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
           Grant grantCriteria = new Grant();
           grantCriteria.setDomainid(did);
           grantCriteria.setUserid(uid);
           List<Object> grants = store.getPOJOs(grantCriteria, false);
           for (int z=0;z<grants.size();z++) {
              Grant grant = (Grant)grants.get(z);
              Role role = new Role();
              role.setRoleid(grant.getRoleid());
              role = (Role)store.getPOJO(role,false);
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

