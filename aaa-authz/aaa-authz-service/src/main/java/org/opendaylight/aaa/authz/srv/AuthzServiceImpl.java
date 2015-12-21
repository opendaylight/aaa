/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authz.srv;

import java.util.List;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.controller.config.yang.config.aaa_authz.srv.Policies;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.ActionType;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authz.ds.rev140722.AuthorizationResponseType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * @author lmukkama Date: 9/2/14
 */
public class AuthzServiceImpl {

    private static List<Policies> listPolicies;

    private static final String WILDCARD_TOKEN = "*";

    public static boolean isAuthorized(LogicalDatastoreType logicalDatastoreType,
            YangInstanceIdentifier yangInstanceIdentifier, ActionType actionType) {

        AuthorizationResponseType authorizationResponseType = AuthzServiceImpl.reqAuthorization(
                actionType, logicalDatastoreType, yangInstanceIdentifier);
        return authorizationResponseType.equals(AuthorizationResponseType.Authorized);
    }

    public static boolean isAuthorized(ActionType actionType) {
        AuthorizationResponseType authorizationResponseType = AuthzServiceImpl
                .reqAuthorization(actionType);
        return authorizationResponseType.equals(AuthorizationResponseType.Authorized);
    }

    public static void setPolicies(List<Policies> policies) {

        AuthzServiceImpl.listPolicies = policies;
    }

    public static AuthorizationResponseType reqAuthorization(ActionType actionType) {

        AuthenticationService authenticationService = AuthzDomDataBroker.getInstance()
                .getAuthService();
        if (authenticationService != null && AuthzServiceImpl.listPolicies != null
                && AuthzServiceImpl.listPolicies.size() > 0) {
            Authentication authentication = authenticationService.get();
            if (authentication != null && authentication.roles() != null
                    && authentication.roles().size() > 0) {
                return checkAuthorization(actionType, authentication);
            }
        }
        return AuthorizationResponseType.NotAuthorized;
    }

    public static AuthorizationResponseType reqAuthorization(ActionType actionType,
            LogicalDatastoreType logicalDatastoreType, YangInstanceIdentifier yangInstanceIdentifier) {

        AuthenticationService authenticationService = AuthzDomDataBroker.getInstance()
                .getAuthService();

        if (authenticationService != null && AuthzServiceImpl.listPolicies != null
                && AuthzServiceImpl.listPolicies.size() > 0) {
            // Authentication Service exists. Can do authorization checks
            Authentication authentication = authenticationService.get();

            if (authentication != null && authentication.roles() != null
                    && authentication.roles().size() > 0) {
                // Authentication claim object exists with atleast one role
                return checkAuthorization(actionType, authentication, logicalDatastoreType,
                        yangInstanceIdentifier);
            }
        }

        return AuthorizationResponseType.Authorized;
    }

    private static AuthorizationResponseType checkAuthorization(ActionType actionType,
            Authentication authentication, LogicalDatastoreType logicalDatastoreType,
            YangInstanceIdentifier yangInstanceIdentifier) {

        for (Policies policy : AuthzServiceImpl.listPolicies) {

            // Action type is compared as string, since its type is string in
            // the config yang. Comparison is case insensitive
            if (authentication.roles().contains(policy.getRole().getValue())
                    && (policy.getResource().getValue().equals(WILDCARD_TOKEN) || policy
                            .getResource().getValue().equals(yangInstanceIdentifier.toString()))
                    && (policy.getAction().toLowerCase()
                            .equals(ActionType.Any.name().toLowerCase()) || actionType.name()
                            .toLowerCase().equals(policy.getAction().toLowerCase()))) {

                return AuthorizationResponseType.Authorized;
            }

        }

        // For helium release we unauthorize other requests.
        return AuthorizationResponseType.NotAuthorized;
    }

    private static AuthorizationResponseType checkAuthorization(ActionType actionType,
            Authentication authentication) {

        for (Policies policy : AuthzServiceImpl.listPolicies) {
            if (authentication.roles().contains(policy.getRole().getValue())
                    && (policy.getAction().equalsIgnoreCase(ActionType.Any.name()) || policy
                            .getAction().equalsIgnoreCase(actionType.name()))) {
                return AuthorizationResponseType.Authorized;
            }
        }
        return AuthorizationResponseType.NotAuthorized;
    }
}
