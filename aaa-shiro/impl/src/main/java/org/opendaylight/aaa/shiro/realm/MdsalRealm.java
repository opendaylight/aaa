/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.Destroyable;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;
import org.opendaylight.aaa.shiro.realm.util.TokenUtils;
import org.opendaylight.aaa.shiro.realm.util.http.header.HeaderUtils;
import org.opendaylight.aaa.shiro.web.env.ThreadLocals;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.Authentication;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.Grant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.Grants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.Roles;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.Users;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Realm based on <code>aaa.yang</code> model.
 */
public class MdsalRealm extends AuthorizingRealm implements Destroyable {
    private static final Logger LOG = LoggerFactory.getLogger(MdsalRealm.class);

    /**
     * InstanceIdentifier for the authentication container.
     */
    private static final DataTreeIdentifier<Authentication> AUTH_TREE_ID = DataTreeIdentifier.create(
            LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Authentication.class));

    private final PasswordHashService passwordHashService;
    private final ListenerRegistration<?> reg;

    private volatile ListenableFuture<Optional<Authentication>> authentication;

    public MdsalRealm() {
        this.passwordHashService = requireNonNull(ThreadLocals.PASSWORD_HASH_SERVICE_TL.get());
        final DataBroker dataBroker = requireNonNull(ThreadLocals.DATABROKER_TL.get());

        try (ReadTransaction tx = dataBroker.newReadOnlyTransaction()) {
            authentication = tx.read(AUTH_TREE_ID.getDatastoreType(), AUTH_TREE_ID.getRootIdentifier());
        }

        reg = dataBroker.registerDataTreeChangeListener(AUTH_TREE_ID,
            (ClusteredDataTreeChangeListener<Authentication>) this::onAuthenticationChanged);

        LOG.info("MdsalRealm created");
    }

    private void onAuthenticationChanged(final Collection<DataTreeModification<Authentication>> changes) {
        final Authentication newVal = Iterables.getLast(changes).getRootNode().getDataAfter();
        LOG.debug("Updating authentication information to {}", newVal);
        authentication = Futures.immediateFuture(Optional.ofNullable(newVal));
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        // the final set or roles to return to the caller;  empty to start
        final Set<String> authRoles = new HashSet<>();
        final ODLPrincipal odlPrincipal = (ODLPrincipal)principalCollection.getPrimaryPrincipal();
        final Optional<Authentication> opt = getAuthenticationContainer();
        if (opt.isPresent()) {
            final Authentication auth = opt.get();

            // iterate through and determine the appropriate roles based on the programmed grants
            final Grants grants = auth.getGrants();
            for (Grant grant : grants.nonnullGrants().values()) {
                if (grant.getUserid().equals(odlPrincipal.getUserId())) {
                    final Roles roles = auth.getRoles();
                    if (roles != null) {
                        for (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214
                                .authentication.roles.Roles role : roles.nonnullRoles().values()) {
                            if (role.getRoleid().equals(grant.getRoleid())) {
                                authRoles.add(role.getRoleid());
                            }
                        }
                    }
                }
            }
        }
        return new SimpleAuthorizationInfo(authRoles);
    }

    /**
     * Utility method to extract the authentication container.
     *
     * @return the <code>authentication</code> container
     */
    private Optional<Authentication> getAuthenticationContainer() {
        try {
            return authentication.get();
        } catch (final InterruptedException | ExecutionException e) {
            LOG.error("Couldn't access authentication container", e);
        }
        return Optional.empty();
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken)
            throws AuthenticationException {

        final String username = TokenUtils.extractUsername(authenticationToken);
        final Optional<Authentication> opt = getAuthenticationContainer();
        if (opt.isPresent()) {
            final Authentication auth = opt.get();
            final Users users = auth.getUsers();
            for (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.authentication.users
                    .Users u : users.nonnullUsers().values()) {
                final String inputUsername = HeaderUtils.extractUsername(username);
                final String domainId = HeaderUtils.extractDomain(username);
                final String inputUserId = String.format("%s@%s", inputUsername, domainId);
                final boolean userEnabled = u.getEnabled();
                if (!userEnabled) {
                    LOG.trace("userId={} is skipped because it is disabled", u.getUserid());
                }
                if (userEnabled && u.getUserid().equals(inputUserId)) {
                    final String inputPassword = TokenUtils.extractPassword(authenticationToken);
                    if (passwordHashService.passwordsMatch(inputPassword, u.getPassword(), u.getSalt())) {
                        final ODLPrincipal odlPrincipal = ODLPrincipalImpl
                                .createODLPrincipal(inputUsername, domainId, inputUserId);
                        return new SimpleAuthenticationInfo(odlPrincipal, inputPassword, getName());
                    }
                }
            }
        }
        LOG.debug("Couldn't access the authentication container");
        throw new AuthenticationException(String.format("Couldn't authenticate %s", username));
    }

    @Override
    public void destroy() {
        reg.close();
    }
}
