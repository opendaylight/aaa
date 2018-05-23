/*
 * Copyright (c) 2016 Orange and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;
//import com.sun.jersey.api.client.config.ClientConfig;
//import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.net.MalformedURLException;
import java.net.URL;
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
//        final String output;
//        final ClientConfig config = new DefaultClientConfig();
//        final Client client = Client.create(config);
//
//        final String hostFromShiro = moonServerURL != null ? moonServerURL.getHost() : null;
//        final String server;
//        if (hostFromShiro != null) {
//            server = hostFromShiro;
//        } else {
//            LOG.debug("moon server was not specified appropriately, cannot authenticate");
//            return null;
//        }
//
//        final int portFromShiro = moonServerURL != null ? moonServerURL.getPort() : -1;
//        final String port;
//        if (portFromShiro > 0) {
//            port = Integer.toString(portFromShiro);
//        } else {
//            LOG.debug("moon server was not specified appropriately, cannot authetnicate");
//            return null;
//        }
//
//        final String url = String.format("http://%s:%s/moon/auth/tokens", server, port);
//        LOG.debug("Moon server is at: {}:{} and will be accessed through {}", server, port, url);
//        final WebResource webResource = client.resource(url);
//        final String input = "{\"username\": \"" + username + "\"," + "\"password\":" + "\"" + password + "\","
//                + "\"project\":" + "\"" + domain + "\"" + "}";
//        final ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);
//        output = response.getEntity(String.class);
//
//        final JsonElement element = new JsonParser().parse(output);
//        if (!element.isJsonObject()) {
//            throw new IllegalStateException("Authentication error: returned output is not a JSON object");
//        }
//
//        final JsonObject object = element.getAsJsonObject();
//        final JsonObject error = object.get("error").getAsJsonObject();
//        if (error != null) {
//            throw new IllegalStateException("Authentication Error : " + error.get("title").getAsString());
//        }
//
//        final JsonElement token = object.get("token");
//        if (token == null) {
//            return null;
//        }
//
//        final String tokenValue = token.getAsString();
//        final String userID = username + "@" + domain;
//
//        final Set<String> userRoles = new LinkedHashSet<>();
//        final JsonElement roles = object.get("roles");
//        if (roles != null) {
//            for (JsonElement role : roles.getAsJsonArray()) {
//                try {
//                    userRoles.add(role.getAsString());
//                } catch (final ClassCastException e) {
//                    LOG.debug("Unable to cast role as String, skipping {}", role, e);
//                }
//            }
//        }
//        return new MoonPrincipal(username, domain, userID, userRoles, tokenValue);
        return null;
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
