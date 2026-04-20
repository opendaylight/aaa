/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.shiro.filters.Oauth2ProxyToken;
import org.opendaylight.aaa.shiro.principal.ODLPrincipalImpl;

/**
 * Realm that relies on user being successfully authorized by identity provider server. Process information
 * forwarded by oauth2-proxy, authorize and authenticate user based on that.
 */
public final class Oauth2ProxyTokenRealm extends AuthorizingRealm {
    private final String ENDPOINT_USERINFO = "https://keycloak:8080/realms/odl-realm/protocol/openid-connect/userinfo";
    private final String ENDPOINT_TOKEN = "http://keycloak:8080/realms/odl-realm/protocol/openid-connect/token";

    public Oauth2ProxyTokenRealm() {
        super();
        setAuthenticationTokenClass(Oauth2ProxyToken.class);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
            throws AuthenticationException {
        if (!(token instanceof Oauth2ProxyToken proxyToken)) {
            throw new AuthenticationException("Only Oauth2ProxyToken is supported by Oauth2ProxyRealm");
        }
        final var user = proxyToken.user();
        final var groups = proxyToken.groups();
        if (groups == null) {
            // Define your Keycloak details
            final var clientID = new ClientID(user);
            final var secret = new Secret("your-client-secret");
            final var clientGrant = new ClientCredentialsGrant();
            final var tokenEndpoint = URI.create(ENDPOINT_TOKEN);

            // Make the request
            TokenRequest request = new TokenRequest(tokenEndpoint, clientID, clientGrant);
            TokenResponse response = TokenResponse.parse(request.toHTTPRequest().send());

            if (!response.indicatesSuccess()) {
                throw new RuntimeException("Failed to get service token");
            }

            final var serviceToken = response.toSuccessResponse().getTokens().getAccessToken();
            final var userInfoRequest = new UserInfoRequest(
                URI.create(ENDPOINT_USERINFO),
                serviceToken
            );

            final var groupsResponse = UserInfoResponse.parse(userInfoRequest.toHTTPRequest().send());
            if (response.indicatesSuccess()) {
                final var userInfo = response.toSuccessResponse().getUserInfo();

                // 3. Extract roles from the 'groups' or 'roles' claim
                // Nimbus allows fetching custom claims easily
                final var roles = userInfo.getStringListClaim("groups");
            }
        }
        final var odlPrincipal = ODLPrincipalImpl.createODLPrincipal(user, null, user,
            parseRoles(proxyToken.groups()));
        return new SimpleAuthenticationInfo(odlPrincipal, token.getCredentials(), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        final var primary = principalCollection.getPrimaryPrincipal();
        if (primary instanceof ODLPrincipal odlPrincipal) {
            return new SimpleAuthorizationInfo(odlPrincipal.getRoles());
        }
        return new SimpleAuthorizationInfo();
    }

    // Parse roles from X-Auth-Request-Groups header. example: role:global-admin,role:odl-application:admin
    // roles are separated by ","
    // each role can have namespace with ":" as separator, we are only interested in the actual value
    private static Set<String> parseRoles(final Enumeration<String> groups) {
        final var out = new HashSet<String>();
        groups.asIterator().forEachRemaining(s -> out.addAll(Arrays.stream(s.split(","))
            .map(p -> p.split(":"))
            .map(p -> p[p.length - 1])
            .collect(Collectors.toSet())));
        return out;
    }
}
