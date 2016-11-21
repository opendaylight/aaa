/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.aaa.ClaimBuilder;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.IdMServiceImpl;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.SHA256Calculator;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.yang.gen.v1.config.aaa.authn.idmlight.rev151204.AAAIDMLightModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An OSGi proxy for the IdmLight server.
 */
public class IdmLightProxy implements CredentialAuth<PasswordCredentials>, IdMService {

    private static final Logger LOG = LoggerFactory.getLogger(IdmLightProxy.class);

    /**
     * claimCache is responsible for storing the active claims per domain.  The
     * outer map is keyed by domain, and the inner map is keyed by
     * <code>PasswordCredentials</code>.
     */
    private static Map<String, Map<PasswordCredentials, Claim>> claimCache = new ConcurrentHashMap<>();

    // adds a store for the default "sdn" domain
    static {
        claimCache.put(IIDMStore.DEFAULT_DOMAIN,
                new ConcurrentHashMap<PasswordCredentials, Claim>());
    }

    @Override
    public Claim authenticate(PasswordCredentials creds) {
        Preconditions.checkNotNull(creds);
        Preconditions.checkNotNull(creds.username());
        Preconditions.checkNotNull(creds.password());
        String domain = creds.domain() == null ? IIDMStore.DEFAULT_DOMAIN : creds.domain();
        // FIXME: Add cache invalidation
        Map<PasswordCredentials, Claim> cache = claimCache.get(domain);
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            claimCache.put(domain, cache);
        }
        Claim claim = cache.get(creds);
        if (claim == null) {
            synchronized (claimCache) {
                claim = cache.get(creds);
                if (claim == null) {
                    claim = dbAuthenticate(creds);
                    if (claim != null) {
                        cache.put(creds, claim);
                    }
                }
            }
        }
        return claim;
    }

    /**
     * Clears the cache of any active claims.
     */
    public static synchronized void clearClaimCache() {
        LOG.info("Clearing the claim cache");
        for (Map<PasswordCredentials, Claim> cache : claimCache.values()) {
            cache.clear();
        }
    }

    private static Claim dbAuthenticate(PasswordCredentials creds) {
        Domain domain = null;
        User user = null;
        String credsDomain = creds.domain() == null ? IIDMStore.DEFAULT_DOMAIN : creds.domain();
        // check to see domain exists
        // TODO: ensure domain names are unique change to 'getDomain'
        LOG.debug("get domain");
        try {
            domain = AAAIDMLightModule.getStore().readDomain(credsDomain);
            if (domain == null) {
                throw new AuthenticationException("Domain :" + credsDomain + " does not exist");
            }
        } catch (IDMStoreException e) {
            throw new AuthenticationException("Error while fetching domain", e);
        }

        // check to see user exists and passes cred check
        try {
            LOG.debug("check user / pwd");
            Users users = AAAIDMLightModule.getStore().getUsers(creds.username(), credsDomain);
            List<User> userList = users.getUsers();
            if (userList.size() == 0) {
                throw new AuthenticationException("User :" + creds.username()
                        + " does not exist in domain " + credsDomain);
            }
            user = userList.get(0);
            if (!SHA256Calculator.getSHA256(creds.password(), user.getSalt()).equals(
                    user.getPassword())) {
                throw new AuthenticationException("UserName / Password not found");
            }

            // get all grants & roles for this domain and user
            LOG.debug("get grants");
            List<String> roles = new ArrayList<>();
            Grants grants = AAAIDMLightModule.getStore().getGrants(domain.getDomainid(),
                    user.getUserid());
            List<Grant> grantList = grants.getGrants();
            for (int z = 0; z < grantList.size(); z++) {
                Grant grant = grantList.get(z);
                Role role = AAAIDMLightModule.getStore().readRole(grant.getRoleid());
                if (role != null) {
                    roles.add(role.getName());
                }
            }

            // build up the claim
            LOG.debug("build a claim");
            ClaimBuilder claim = new ClaimBuilder();
            claim.setUserId(user.getUserid().toString());
            claim.setUser(creds.username());
            claim.setDomain(credsDomain);
            for (int z = 0; z < roles.size(); z++) {
                claim.addRole(roles.get(z));
            }
            return claim.build();
        } catch (IDMStoreException se) {
            throw new AuthenticationException("idm data store exception :" + se.toString() + se);
        }
    }

    @Override
    public List<String> listDomains(String userId) {
        return new IdMServiceImpl(AAAIDMLightModule.getStore()).listDomains(userId);
    }

    @Override
    public List<String> listRoles(String userId, String domainName) {
        return new IdMServiceImpl(AAAIDMLightModule.getStore()).listRoles(userId, domainName);
    }

    @Override
    public List<String> listUserIDs() throws IDMStoreException {
        return new IdMServiceImpl(AAAIDMLightModule.getStore()).listUserIDs();
    }
}
