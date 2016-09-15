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
 * MoonRealm is a Shiro Realm that authenticates users from OPNFV/moon platform
 * @author Alioune BA alioune.ba@orange.com
 *
 */
public class MoonRealm extends AuthorizingRealm{

    private static final Logger LOG = LoggerFactory.getLogger(MoonRealm.class);
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String username = "";
        String password = "";
        String domain = "sdn";
        username = (String) authenticationToken.getPrincipal();
        final UsernamePasswordToken upt = (UsernamePasswordToken) authenticationToken;
        password =  new String(upt.getPassword());
        final MoonPrincipal moonPrincipal = moonAuthenticate(username,password,domain);
        if (moonPrincipal!=null){
            return new SimpleAuthenticationInfo(moonPrincipal, password.toCharArray(),getName());
        }else{
            return null;
        }
    }

    public MoonPrincipal moonAuthenticate(String username, String password, String domain){
        String output = "";
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create(config);
        JSONTokener tokener;
        JSONObject object =null;
        Set<String> UserRoles = new LinkedHashSet<>();

        String server = System.getenv("MOON_SERVER_ADDR");
        String port = System.getenv("MOON_SERVER_PORT");
        String URL = "http://" +server+ ":" +port+ "/moon/auth/tokens";
        LOG.debug("Moon server is at: {} ", server);
        WebResource webResource = client.resource(URL);
        String input = "{\"username\": \""+ username + "\"," + "\"password\":" + "\"" + password + "\"," + "\"project\":" + "\"" + domain + "\"" + "}";
        ClientResponse response = webResource.type("application/json").post(ClientResponse.class, input);
        output = response.getEntity(String.class);
        tokener = new JSONTokener(output);
        object = new JSONObject(tokener);
        try {
            if (object.getString("token")!=null){
                String token = object.getString("token");
                String userID = username+"@"+domain;
                for (int i=0; i< object.getJSONArray("roles").length(); i++){
                    UserRoles.add((String) object.getJSONArray("roles").get(i));
                }
                MoonPrincipal principal = new MoonPrincipal(username,domain,userID,UserRoles,token);
                return principal;
            }
        }catch (JSONException e){
            throw new IllegalStateException("Authentication Error : "+ object.getJSONObject("error").getString("title"));
        }
        return null;
    }

}
