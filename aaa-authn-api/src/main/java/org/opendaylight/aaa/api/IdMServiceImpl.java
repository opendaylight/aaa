/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.api.model.Grants;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IdMService implementation.
 *
 * @author Michael Vorburger, partially based on code refactored from IdmLightProxy and StoreBuilder
 */
public class IdMServiceImpl implements IdMService {

    private static final Logger LOG = LoggerFactory.getLogger(IdMServiceImpl.class);

    private final IIDMStore repository;

    public IdMServiceImpl(IIDMStore repository) {
        super();
        this.repository = repository;
    }

    @Override
    public List<String> listDomains(String userId) {
        LOG.debug("list Domains for userId: {}", userId);
        List<String> domains = new ArrayList<>();
        try {
            Grants grants = repository.getGrants(userId);
            List<Grant> grantList = grants.getGrants();
            for (Grant grant : grantList) {
                Domain domain = repository.readDomain(grant.getDomainid());
                domains.add(domain.getName());
            }
            return domains;
        } catch (IDMStoreException se) {
            LOG.warn("error getting domains", se);
            return domains;
        }
    }

    @Override
    public List<String> listRoles(String userId, String domainName) {
        LOG.debug("listRoles for userId={} on domain={}", userId, domainName);
        List<String> roles = new ArrayList<>();

        try {
            // find domain name for specified domain name
            String did = null;
            try {
                Domain domain = repository.readDomain(domainName);
                if (domain == null) {
                    LOG.debug("DomainName: {} Not found!", domainName);
                    return roles;
                }
                did = domain.getDomainid();
            } catch (IDMStoreException e) {
                return roles;
            }

            // find all grants for uid and did
            Grants grants = repository.getGrants(did, userId);
            List<Grant> grantList = grants.getGrants();
            for (int z = 0; z < grantList.size(); z++) {
                Grant grant = grantList.get(z);
                Role role = repository.readRole(grant.getRoleid());
                roles.add(role.getName());
            }

            return roles;
        } catch (IDMStoreException se) {
            LOG.warn("error getting roles ", se);
            return roles;
        }
    }

    @Override
    public List<String> listUserIDs() throws IDMStoreException {
        List<User> users = repository.getUsers().getUsers();
        return users.stream().map(user -> user.getName()).collect(Collectors.toList());
    }

}
