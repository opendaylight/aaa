/*
 * Copyright (c) 2016 Orange and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static java.util.Objects.requireNonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.opendaylight.aaa.shiro.moon.MoonPrincipal;
import org.opendaylight.aaa.web.servlet.ServletSupport;
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

    private final ServletSupport support;

    private URL moonServerURL;

    MoonRealm(final ServletSupport support) {
        this.support = requireNonNull(support);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            final AuthenticationToken authenticationToken) throws AuthenticationException {
        final String username;
        final String password;
        final String domain = MOON_DEFAULT_DOMAIN;

        try {
            username = (String) authenticationToken.getPrincipal();
        } catch (final ClassCastException e) {
            LOG.debug("doGetAuthenticationInfo() failed because the principal couldn't be cast as a String", e);
            throw e;
        }

        final UsernamePasswordToken upt;
        try {
            upt = (UsernamePasswordToken) authenticationToken;
        } catch (final ClassCastException e) {
            LOG.debug("doGetAuthenticationInfo() failed because the token was not a UsernamePasswordToken", e);
            throw e;
        }

        password = new String(upt.getPassword());

        final MoonPrincipal moonPrincipal = moonAuthenticate(username, password, domain);
        if (moonPrincipal != null) {
            return new SimpleAuthenticationInfo(moonPrincipal, password.toCharArray(), getName());
        } else {
            return null;
        }
    }

    public MoonPrincipal moonAuthenticate(final String username, final String password, final String domain) {
        final String hostFromShiro = moonServerURL != null ? moonServerURL.getHost() : null;
        final String server;
        if (hostFromShiro != null) {
            server = hostFromShiro;
        } else {
            LOG.debug("moon server was not specified appropriately, cannot authenticate");
            return null;
        }

        final int portFromShiro = moonServerURL != null ? moonServerURL.getPort() : -1;
        final String port;
        if (portFromShiro > 0) {
            port = Integer.toString(portFromShiro);
        } else {
            LOG.debug("moon server was not specified appropriately, cannot authetnicate");
            return null;
        }

        final String url = String.format("http://%s:%s/moon/auth/tokens", server, port);
        LOG.debug("Moon server is at: {}:{} and will be accessed through {}", server, port, url);

        final Client client = support.createClientBuilder().build();
        final WebTarget webResource = client.target(url);
        final String input = "{\"username\": \"" + username + "\"," + "\"password\":" + "\"" + password + "\","
                + "\"project\":" + "\"" + domain + "\"" + "}";

        final InputStream response = webResource.request("application/json")
                .post(Entity.text(input), ClientResponseContext.class).getEntityStream();
        client.close();

        final JsonElement element = new JsonParser().parse(new InputStreamReader(response, StandardCharsets.UTF_8));
        if (!element.isJsonObject()) {
            throw new IllegalStateException("Authentication error: returned output is not a JSON object");
        }

        final JsonObject object = element.getAsJsonObject();
        final JsonObject error = object.get("error").getAsJsonObject();
        if (error != null) {
            throw new IllegalStateException("Authentication Error : " + error.get("title").getAsString());
        }

        final JsonElement token = object.get("token");
        if (token == null) {
            return null;
        }

        final String tokenValue = token.getAsString();
        final String userID = username + "@" + domain;

        final Set<String> userRoles = new LinkedHashSet<>();
        final JsonElement roles = object.get("roles");
        if (roles != null) {
            for (JsonElement role : roles.getAsJsonArray()) {
                try {
                    userRoles.add(role.getAsString());
                } catch (final ClassCastException e) {
                    LOG.debug("Unable to cast role as String, skipping {}", role, e);
                }
            }
        }

        return new MoonPrincipal(username, domain, userID, userRoles, tokenValue);
    }

    /**
     * Injected from <code>shiro.ini</code>.
     *
     * @param moonServerURL specified in <code>shiro.ini</code>
     */
    public void setMoonServerURL(final String moonServerURL) {
        try {
            this.moonServerURL = new URL(moonServerURL);
        } catch (final MalformedURLException e) {
            LOG.warn("The moon server URL could not be parsed", e);
        }
    }
}
