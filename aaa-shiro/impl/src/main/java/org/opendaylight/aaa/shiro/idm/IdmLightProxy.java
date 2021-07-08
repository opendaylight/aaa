/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.idm;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.ClaimCache;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.IdMServiceImpl;
import org.opendaylight.aaa.api.PasswordCredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.api.tokenauthrealm.auth.ClaimBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An OSGi proxy for the IdmLight server.
 */
@Singleton
public class IdmLightProxy implements PasswordCredentialAuth, IdMService, ClaimCache {
    private static final Logger LOG = LoggerFactory.getLogger(IdmLightProxy.class);

    /**
     * Responsible for storing the active claims per domain. The outer map is keyed by domain, and the inner map is
     * keyed by <code>PasswordCredentials</code>.
     */
    private final Map<String, Map<PasswordCredentials, Claim>> claimCache = new ConcurrentHashMap<>();

    private final IIDMStore idmStore;
    private final PasswordHashService passwordService;

    @Inject
    public IdmLightProxy(final IIDMStore idmStore, final PasswordHashService passwordService) {
        this.idmStore = idmStore;
        this.passwordService = requireNonNull(passwordService);
    }

    @Override
    public Claim authenticate(final PasswordCredentials creds) {
        requireNonNull(creds);
        requireNonNull(creds.username());
        requireNonNull(creds.password());
        String domain = creds.domain() == null ? IIDMStore.DEFAULT_DOMAIN : creds.domain();

        // FIXME: Add cache invalidation
        return claimCache.computeIfAbsent(domain, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(creds, this::dbAuthenticate);
    }

    /**
     * Clears the cache of any active claims.
     */
    @Override
    public void clear() {
        LOG.info("Clearing the claim cache");
        claimCache.clear();
    }

    private Claim dbAuthenticate(final PasswordCredentials creds) {
        Domain domain = null;
        User user = null;
        String credsDomain = creds.domain() == null ? IIDMStore.DEFAULT_DOMAIN : creds.domain();
        // check to see domain exists
        // TODO: ensure domain names are unique change to 'getDomain'
        LOG.debug("get domain");
        try {
            domain = idmStore.readDomain(credsDomain);
            if (domain == null) {
                throw new AuthenticationException("Domain :" + credsDomain + " does not exist");
            }
        } catch (IDMStoreException e) {
            throw new AuthenticationException("Error while fetching domain", e);
        }

        // check to see user exists and passes cred check
        try {
            LOG.debug("check user / pwd");
            Users users = idmStore.getUsers(creds.username(), credsDomain);
            List<User> userList = users.getUsers();
            if (userList.size() == 0) {
                throw new AuthenticationException("User :" + creds.username()
                        + " does not exist in domain " + credsDomain);
            }
            user = userList.get(0);
            if (!passwordService.passwordsMatch(creds.password(), user.getPassword(), user.getSalt())) {
                throw new AuthenticationException("UserName / Password not found");
            }
            if (!user.isEnabled()) {
                throw new AuthenticationException("Account is disabled");
            }

            // get all grants & roles for this domain and user
            LOG.debug("get grants");
            List<String> roles = new ArrayList<>();
            Grants grants = idmStore.getGrants(domain.getDomainid(), user.getUserid());
            for (Grant grant : grants.getGrants()) {
                Role role = idmStore.readRole(grant.getRoleid());
                if (role != null) {
                    roles.add(role.getName());
                }
            }

            // build up the claim
            LOG.debug("build a claim");
            ClaimBuilder claim = new ClaimBuilder();
            claim.setUserId(user.getUserid());
            claim.setUser(creds.username());
            claim.setDomain(credsDomain);
            for (String role : roles) {
                claim.addRole(role);
            }
            return claim.build();
        } catch (IDMStoreException se) {
            throw new AuthenticationException("idm data store exception :" + se.toString() + se);
        }
    }

    @Override
    public List<String> listDomains(final String userId) {
        return new IdMServiceImpl(idmStore).listDomains(userId);
    }

    @Override
    public List<String> listRoles(final String userId, final String domainName) {
        return new IdMServiceImpl(idmStore).listRoles(userId, domainName);
    }

    @Override
    public List<String> listUserIDs() throws IDMStoreException {
        return new IdMServiceImpl(idmStore).listUserIDs();
    }
}
