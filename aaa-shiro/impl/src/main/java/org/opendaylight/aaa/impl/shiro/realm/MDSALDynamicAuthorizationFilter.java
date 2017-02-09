/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.realm;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.base.Optional;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.opendaylight.aaa.impl.AAAShiroProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.HttpAuthorization;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.Policies;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.permission.Permissions;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a dynamic authorization mechanism for restful web services with permission grain
 * scope.  <code>aaa.yang</code> defines the model for this filtering mechanism.
 * This model exposes the ability to manipulate policy information for specific paths
 * based on a tuple of (role, http_permission_list).
 *
 * This mechanism will only work when put behind <code>authcBasic</code>
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class MDSALDynamicAuthorizationFilter extends AuthorizationFilter {

    private static final Logger LOG = LoggerFactory.getLogger(MDSALDynamicAuthorizationFilter.class);

    private static final InstanceIdentifier<HttpAuthorization> AUTHZ_CONTAINER_IID =
            InstanceIdentifier.builder(HttpAuthorization.class).build();

    public static Optional<HttpAuthorization> getHttpAuthzContainer(final DataBroker dataBroker)
            throws ExecutionException, InterruptedException {

        final ReadOnlyTransaction ro = dataBroker.newReadOnlyTransaction();
        final CheckedFuture<Optional<HttpAuthorization>, ReadFailedException> result =
                ro.read(LogicalDatastoreType.CONFIGURATION, AUTHZ_CONTAINER_IID);
        return result.get();
    }

    @Override
    public boolean isAccessAllowed(final ServletRequest request, final ServletResponse response,
                                   final Object mappedValue) {
        return isAccessAllowed(request, response, mappedValue, AAAShiroProvider.getInstance().getDataBroker());
    }

    public boolean isAccessAllowed(final ServletRequest request, final ServletResponse response,
                                   final Object mappedValue, final DataBroker dataBroker) {

        final Subject subject = getSubject(request, response);
        final HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        final String requestURI = httpServletRequest.getRequestURI();
        LOG.debug("isAccessAllowed for user={} to requestURI={}", subject, requestURI);

        final Optional<HttpAuthorization> authorizationOptional;
        try {
            authorizationOptional = getHttpAuthzContainer(dataBroker);
        } catch(ExecutionException | InterruptedException e) {
            // Something went completely wrong trying to read the authz container.  Deny access.
            LOG.debug("Error accessing the Http Authz Container", e);
            return false;
        }

        if (!authorizationOptional.isPresent()) {
            // The authorization container does not exist-- hence no authz rules are present
            // Allow access.
            LOG.debug("Authorization Container does not exist");
            return true;
        }


        final HttpAuthorization httpAuthorization = authorizationOptional.get();
        final Policies policies = httpAuthorization.getPolicies();
        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.policies.Policies> policiesList =
                policies.getPolicies();
        if(policiesList.isEmpty()) {
            // The authorization container exists, but no rules are present.  Allow access.
            LOG.debug("Exiting successfully early since no authorization rules exist");
            return true;
        }

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.policies.Policies policy :
                policiesList) {
            final String resource = policy.getResource();
            final boolean pathsMatch = pathsMatch(resource, requestURI);
            if (pathsMatch) {
                LOG.debug("paths match for pattern={} and requestURI={}", resource, requestURI);
                final String method = httpServletRequest.getMethod();
                LOG.trace("method={}", method);
                final List<Permissions> permissions = policy.getPermissions();
                for (Permissions permission : permissions) {
                    final String role = permission.getRole();
                    LOG.trace("role={}", role);
                    final List<Permissions.Actions> actions = permission.getActions();
                    for(Permissions.Actions action : actions) {
                        LOG.trace("action={}", action.getName());
                        if(action.getName().equalsIgnoreCase(method)) {
                            final boolean hasRole = subject.hasRole(role);
                            LOG.trace("hasRole({})={}", role, hasRole);
                            if (hasRole) {
                                return true;
                            }
                        }
                    }
                }
                LOG.debug("couldn't authorize the user for access");
                return false;
            }
        }
        LOG.debug("successfully authorized the user for access");
        return true;
    }
}