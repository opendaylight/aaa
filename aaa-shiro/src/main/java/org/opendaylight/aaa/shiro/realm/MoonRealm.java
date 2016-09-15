/*
 * Copyright (c) 2016 Orange and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
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
    private static final String MOON_SERVER_ADDRESS_ENV_VAR_NAME = "MOON_SERVER_ADDR";
    private static final String MOON_SERVER_PORT_ENV_VAR_NAME = "MOON_SERVER_PORT";

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken authenticationToken) throws AuthenticationException {
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

        final MoonPrincipal moonPrincipal = moonAuthenticate(username,password,domain);
        if (moonPrincipal != null){
            return new SimpleAuthenticationInfo(moonPrincipal, password.toCharArray(),getName());
        } else {
            return null;
        }
    }

    public MoonPrincipal moonAuthenticate(final String username, final String password, final String domain) {
        final String output;
        final ClientConfig config = new DefaultClientConfig();
        final Client client = Client.create(config);
        final JSONTokener tokener;
        final JSONObject object;
        final Set<String> userRoles = new LinkedHashSet<>();

        final String server = System.getenv(MOON_SERVER_ADDRESS_ENV_VAR_NAME);
        final String port = System.getenv(MOON_SERVER_PORT_ENV_VAR_NAME);
        final String url = String.format("http://%s:%s/moon/auth/tokens", server, port);
        LOG.debug("Moon server is at: {}:{} and will be accessed through {}", server, port, url);
        final WebResource webResource = client.resource(URL);
        final String input = "{\"username\": \""+ username + "\"," + "\"password\":" + "\"" + password + "\"," + "\"project\":" + "\"" + domain + "\"" + "}";
        final ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);
        output = response.getEntity(String.class);
        tokener = new JSONTokener(output);
        object = new JSONObject(tokener);

        try {
            if (object.getString("token") != null) {
                final String token = object.getString("token");
                final String userID = username + "@" +domain;
                for (Object maybeRole : object.getJSONArray("roles")) {
                    try {
                        userRoles.add((String) maybeRole);
                    } catch (final ClassCastException e) {
                        LOG.debug("Skipping adding role: {} as it could not be cast as a String", maybeRole, e);
                    }
                }
                return new MoonPrincipal(username,domain,userID,userRoles,token);
            }
        } catch (final JSONException e) {
            throw new IllegalStateException("Authentication Error : " + object.getJSONObject("error").getString("title"));
        }
        return null;
    }
}
