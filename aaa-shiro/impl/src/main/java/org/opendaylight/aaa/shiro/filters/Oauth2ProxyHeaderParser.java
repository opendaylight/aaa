/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import java.util.Set;
import javax.servlet.ServletRequest;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Parses {@code X-Forwarded-User}/{@code X-Forwarded-Groups} proxy headers into an
 * {@link Oauth2ProxyHeaderToken}, applying the limits and character whitelist from a given
 * {@link Oauth2ProxyHeaderParserConfig}.
 *
 * <p>Used by {@link Oauth2ProxyHeaderFilter}, and safe for other consumers of the same proxy headers
 * (e.g. other filters, or other Karaf features fronted by the same OAuth2-Proxy) to reuse instead of
 * duplicating the validation/sanitization logic.
 *
 * <p>The default implementation is registered as an OSGi service, built from the
 * {@link Oauth2ProxyHeaderParserConfig} service so its limits stay consistent with what is actually
 * configured: external consumers should look the parser service up instead of constructing their own.
 * A configuration change re-registers this service with the new limits, so per-request lookups always
 * observe the current configuration. Blueprint consumers get the same freshness through the damped
 * {@code <reference>} proxy, which transparently re-binds to the replacement service.
 *
 * <p>How "current" is maintained differs by injection style, though, and it is worth knowing which one
 * applies. A Blueprint {@code <reference>} injects a damped proxy: a single field captured once, e.g. in
 * a constructor, is safe to keep for the life of the bean, since every call through it is dispatched to
 * whatever implementation is currently registered — no restart involved. A plain Declarative Services
 * {@literal @Reference} (the only option available for constructor injection, since a reference used that
 * way must use DS's static reference policy — an OSGi binding-policy concept, unrelated to the Java
 * {@code static} keyword) instead binds directly to a service instance: when a configuration change
 * re-registers this service, the consuming component is deactivated and reactivated to rebind, so its
 * field does end up current, but the component itself briefly restarts along with it. DS consumers who
 * want the same restart-free, always-current access that Blueprint gives can inject a reference proxy
 * instead of the service directly — e.g. a {@code ComponentServiceObjects}
 * for this interface, or a {@code ServiceTracker} — and call {@code getService()} per use; that resolves
 * to the live implementation on every call, the same way the Blueprint proxy does.
 */
@NonNullByDefault
public interface Oauth2ProxyHeaderParser {
    /**
     * Proxy header containing username.
     *
     * <p>ODL is set as upstream of OAuth2-Proxy thus X-Forwarded-User instead of X-Auth-Request-User header
     */
    String PROXY_HEADER_USER = "X-Forwarded-User";
    /**
     * Proxy header containing user roles.
     *
     * <p>ODL is set as upstream of OAuth2-Proxy thus X-Forwarded-Groups instead of X-Auth-Request-Groups header
     */
    String PROXY_HEADER_GROUPS = "X-Forwarded-Groups";

    /**
     * Parses both proxy headers of the given request into a single token.
     *
     * @param request A {@link ServletRequest} request we are processing
     * @return An {@link Oauth2ProxyHeaderToken} built from the request's proxy headers
     */
    Oauth2ProxyHeaderToken parseToken(ServletRequest request);

    /**
     * Parses user from {@code PROXY_HEADER_USER} header.
     *
     * @param request A {@link ServletRequest} request we are processing
     * @return A single sanitized user
     */
    @Nullable String parseUser(ServletRequest request);

    /**
     * Parses roles from {@code PROXY_HEADER_GROUPS} header.
     *
     * <p>Example: role:global-admin,role:odl-application:admin
     * roles are separated by "," and each role can have namespace with ":" as separator
     * we want to get role with its namespace but without "role:" at the beginning.
     *
     * @param request A {@link ServletRequest} request we are processing
     * @return Set of parsed roles
     */
    Set<String> parseRolesHeader(ServletRequest request);
}
