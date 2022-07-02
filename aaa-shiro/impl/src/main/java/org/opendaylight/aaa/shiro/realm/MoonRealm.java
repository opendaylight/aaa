/*
 * Copyright (c) 2016 Orange and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonParser;
import java.net.MalformedURLException;
import java.net.URL;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.shiro.moon.MoonPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MoonRealm is a Shiro Realm that authenticates users from OPNFV/moon platform.
 *
 * @author Alioune BA alioune.ba@orange.com
 */
public class MoonRealm extends AuthorizingRealm {
    private static final Logger LOG = LoggerFactory.getLogger(MoonRealm.class);
    private static final String MOON_DEFAULT_DOMAIN = "sdn";

    private URL moonServerURL;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken)
            throws AuthenticationException {
        final var principal = authenticationToken.getPrincipal();
        if (!(principal instanceof String)) {
            throw new AuthenticationException("Non-string principal " + principal);
        }

        if (!(authenticationToken instanceof UsernamePasswordToken)) {
            throw new AuthenticationException("Token is not UsernamePasswordToken: " + authenticationToken);
        }

        final var password = new String(((UsernamePasswordToken) authenticationToken).getPassword());
        // FIXME: make the domain name configurable
        final var moonPrincipal = moonAuthenticate((String) principal, password, MOON_DEFAULT_DOMAIN);
        return moonPrincipal == null ? null
            : new SimpleAuthenticationInfo(moonPrincipal, password.toCharArray(), getName());
    }

    public MoonPrincipal moonAuthenticate(final String username, final String password, final String domain) {
        final String server = moonServerURL != null ? moonServerURL.getHost() : null;
        if (server == null) {
            LOG.debug("moon server was not specified appropriately, cannot authenticate");
            return null;
        }

        final int portFromShiro = moonServerURL != null ? moonServerURL.getPort() : -1;
        if (portFromShiro <= 0) {
            LOG.debug("moon server was not specified appropriately, cannot authetnicate");
            return null;
        }

        final var port = Integer.toString(portFromShiro);
        final var url = String.format("http://%s:%s/moon/auth/tokens", server, port);
        LOG.debug("Moon server is at: {}:{} and will be accessed through {}", server, port, url);

        final String output = ClientBuilder.newClient()
            .target(url)
            .request(MediaType.APPLICATION_JSON)
            .post(
                // FIXME: String literal when we have JDK17
                Entity.entity("{\"username\": \"" + username + "\",\n"
                    + "  \"password\": \"" + password + "\",\n"
                    + "  \"project\": \"" + domain + "\"\n}",
                    MediaType.APPLICATION_JSON),
                String.class);

        final var element = JsonParser.parseString(output);
        if (!element.isJsonObject()) {
            throw new IllegalStateException("Authentication error: returned output is not a JSON object");
        }

        final var object = element.getAsJsonObject();
        final var error = object.get("error").getAsJsonObject();
        if (error != null) {
            throw new IllegalStateException("Authentication Error : " + error.get("title").getAsString());
        }

        final var token = object.get("token");
        if (token == null) {
            return null;
        }

        final var userRoles = ImmutableSet.<String>builder();
        final var roles = object.get("roles");
        if (roles != null) {
            for (var role : roles.getAsJsonArray()) {
                try {
                    userRoles.add(role.getAsString());
                } catch (ClassCastException e) {
                    LOG.debug("Unable to cast role as String, skipping {}", role, e);
                }
            }
        }
        return new MoonPrincipal(username, domain, username + "@" + domain, userRoles.build(), token.getAsString());
    }

    /**
     * Injected from {@code shiro.ini}.
     *
     * @param moonServerURL specified in {@code shiro.ini}
     */
    public void setMoonServerURL(final String moonServerURL) {
        try {
            this.moonServerURL = new URL(moonServerURL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot parse moon server URL \"" + moonServerURL + "\"", e);
        }
    }
}
