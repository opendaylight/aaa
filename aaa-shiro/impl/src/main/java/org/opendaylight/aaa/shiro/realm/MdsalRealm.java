/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.HashSet;
import java.util.Optional;
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
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.Authentication;
import org.opendaylight.yangtools.concepts.Registration;
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

    private static final ThreadLocal<PasswordHashService> PASSWORD_HASH_SERVICE_TL = new ThreadLocal<>();
    private static final ThreadLocal<DataBroker> DATABROKER_TL = new ThreadLocal<>();

    private final PasswordHashService passwordHashService;
    private final Registration reg;

    private volatile ListenableFuture<Optional<Authentication>> authentication;

    public MdsalRealm() {
        this(verifyLoad(PASSWORD_HASH_SERVICE_TL), verifyLoad(DATABROKER_TL));
    }

    private static <T> T verifyLoad(final ThreadLocal<T> threadLocal) {
        return verifyNotNull(threadLocal.get(), "MdsalRealm not prepared for loading");
    }

    public MdsalRealm(final PasswordHashService passwordHashService, final DataBroker dataBroker) {
        this.passwordHashService = requireNonNull(passwordHashService);

        try (ReadTransaction tx = dataBroker.newReadOnlyTransaction()) {
            authentication = tx.read(AUTH_TREE_ID.getDatastoreType(), AUTH_TREE_ID.getRootIdentifier());
        }

        reg = dataBroker.registerDataListener(AUTH_TREE_ID, this::onAuthenticationChanged);

        LOG.info("MdsalRealm created");
    }

    public static Registration prepareForLoad(final PasswordHashService passwordHashService,
            final DataBroker dataBroker) {
        PASSWORD_HASH_SERVICE_TL.set(requireNonNull(passwordHashService));
        DATABROKER_TL.set(requireNonNull(dataBroker));
        return () -> {
            PASSWORD_HASH_SERVICE_TL.remove();
            DATABROKER_TL.remove();
        };
    }

    private void onAuthenticationChanged(final Authentication data) {
        LOG.debug("Updating authentication information to {}", data);
        authentication = Futures.immediateFuture(Optional.ofNullable(data));
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        // the final set or roles to return to the caller;  empty to start
        final var authRoles = new HashSet<String>();
        final var odlPrincipal = (ODLPrincipal) principalCollection.getPrimaryPrincipal();
        getAuthenticationContainer().ifPresent(auth -> {
            // iterate through and determine the appropriate roles based on the programmed grants
            final var grants = auth.getGrants();
            for (var grant : grants.nonnullGrants().values()) {
                if (grant.getUserid().equals(odlPrincipal.getUserId())) {
                    final var roles = auth.getRoles();
                    if (roles != null) {
                        for (var role : roles.nonnullRoles().values()) {
                            if (role.getRoleid().equals(grant.getRoleid())) {
                                authRoles.add(role.getRoleid());
                            }
                        }
                    }
                }
            }
        });
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
        final var username = TokenUtils.extractUsername(authenticationToken);
        final var opt = getAuthenticationContainer();
        if (opt.isPresent()) {
            final var auth = opt.orElseThrow();
            final var users = auth.getUsers();
            for (var u : users.nonnullUsers().values()) {
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
                        return new SimpleAuthenticationInfo(
                            ODLPrincipalImpl.createODLPrincipal(inputUsername, domainId, inputUserId), inputPassword,
                            getName());
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
