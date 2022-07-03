/*
 * Copyright (c) 2016 Orange and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonParser;
import java.net.MalformedURLException;
import java.net.URL;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
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
import org.opendaylight.aaa.web.servlet.ServletSupport;
import org.opendaylight.yangtools.concepts.Registration;
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

    private static final ThreadLocal<ServletSupport> SERVLET_SUPPORT_TL = new ThreadLocal<>();

    private final ServletSupport servletSupport;
    private volatile WebTarget moonServer;

    public MoonRealm() {
        this(verifyNotNull(SERVLET_SUPPORT_TL.get(), "MoonRealm loading not prepared"));
    }

    public MoonRealm(final ServletSupport servletSupport) {
        this.servletSupport = requireNonNull(servletSupport);
    }

    public static Registration prepareForLoad(final ServletSupport jaxrsSupport) {
        SERVLET_SUPPORT_TL.set(requireNonNull(jaxrsSupport));
        return () -> SERVLET_SUPPORT_TL.remove();
    }

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
        final var moon = moonServer;
        if (moon == null) {
            LOG.debug("moon server not specified, cannot authenticate");
            return null;
        }

        final var element = JsonParser.parseString(moon.request(MediaType.APPLICATION_JSON).post(
            // FIXME: String literal when we have JDK17
            Entity.entity("{\"username\": \"" + username + "\",\n"
                + "  \"password\": \"" + password + "\",\n"
                + "  \"project\": \"" + domain + "\"\n}",
                MediaType.APPLICATION_JSON),
            String.class));
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
     * @throws NullPointerException If {@code moonServerURL} is {@code null}
     * @throws IllegalArgumentException If the given string violates RFC&nbsp;2396 or it does not specify a host
     *                                  and a port.
     */
    public void setMoonServerURL(final String moonServerURL) {
        final URL url;
        try {
            url = new URL(moonServerURL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }

        final var uriHost = url.getHost();
        checkArgument(uriHost != null, "moon host not specified in %s", url);
        final var uriPort = url.getPort();
        checkArgument(uriPort >= 0, "moon port not specified in %s", url);

        final var port = Integer.toString(uriPort);
        // FIXME: allow HTTPS!
        // FIXME: allow authentication: and that really means configuring a Client!
        final var server = String.format("http://%s:%s/moon/auth/tokens", uriHost, port);
        LOG.debug("Moon server is at: {}:{} and will be accessed through {}", uriHost, port, server);
        moonServer = servletSupport.newClientBuilder().build().target(server);
    }
}
