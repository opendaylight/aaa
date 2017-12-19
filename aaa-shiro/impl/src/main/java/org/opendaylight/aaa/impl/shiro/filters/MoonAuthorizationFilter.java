/*
 * Copyright (c) 2017 Orange and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.filters;

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
import org.opendaylight.aaa.impl.shiro.principal.ODLPrincipalImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.net.URL;
import java.util.*;
import org.json.*;
import java.io.*;

/**
 * Provides a dynamic authorization mechanism for restful web services from Moon platform
 *
 * This mechanism will only work when put behind <code>authcBasic</code>
 */
public class MoonAuthorizationFilter extends AuthorizationFilter {

    private String moonServerURL = "http://localhost:31003";

    private static final Logger LOG = LoggerFactory.getLogger(MoonAuthorizationFilter.class);

    @Override
    public boolean isAccessAllowed(final ServletRequest request, final ServletResponse response,
                                   final Object mappedValue) {
        final Subject subject = getSubject(request, response);
        final ODLPrincipalImpl principal = (ODLPrincipalImpl) subject.getPrincipal();
        final String username = principal.getUsername();
        final HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        final String requestURI = httpServletRequest.getRequestURI();
        final String method = httpServletRequest.getMethod();
        
        return RestAuthorization(username, requestURI, method);
    }

    private boolean RestAuthorization(final String username, final String requestURI, final String method) {
        final String fullUrl = String.format("%s/interface/authz/deny/project_id/%s/object_name/%s", moonServerURL, username, method);
        //final String fullUrl = String.format("%s/interface/authz/grant/project_id/%s/object_name/%s", moonServerURL, username, method);
        final ClientConfig config = new DefaultClientConfig();
        final Client client = Client.create(config);
        final WebResource webResource = client.resource(fullUrl);
        final ClientResponse response = webResource.type("application/json").get(ClientResponse.class);
        final String output = response.getEntity(String.class);
        final JSONTokener tokener = new JSONTokener(output);
        final JSONObject object = new JSONObject(tokener);

	boolean result;
        try {
            result = (Boolean) object.get("result");
        } catch (NullPointerException e){
            return false;
        }
        return result;
    }

     /**
     * The URL of the Moon server. Injected from
     * <code>aaa-app-config.xml</code>.
     *
     * @param url the URL specified in <code>aaa-app-config.xml</code>.
     */
    public void setUrl(final String url) {
        moonServerURL = url;
    }
}
