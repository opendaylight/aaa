/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.sts;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.TokenStore;

import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

/**
 * A base test class for the secure token service.
 *
 * @author liemmn
 *
 */
public abstract class AbstractAuthTest extends JerseyTest {
    protected static final String GOOD_TOKEN = "9b01b7cf-8a49-346d-8c47-6a61193e2b60";
    protected static final String BAD_TOKEN = "9b01b7cf-8a49-346d-8c47-6a611badbeef";
    protected static Authentication auth = new AuthenticationBuilder()
            .setUserId("1234").setUser("Bob").addRole("admin")
            .addRole("user").setDomain("tenantX")
            .setExpiration(System.currentTimeMillis() + 1000).build();

    static {
        ServiceLocator.INSTANCE.as = new AuthenticationService() {
            ThreadLocal<Authentication> ctx = new ThreadLocal<>();

            @Override
            public Authentication get() {
                return ctx.get();
            }

            @Override
            public void set(Authentication auth) {
                ctx.set(auth);
            }

            @Override
            public void clear() {
                ctx.remove();
            }
        };
        ServiceLocator.INSTANCE.ts = new TokenStore() {
            Map<String, Authentication> tokens = new HashMap<>();

            @Override
            public boolean delete(String token) {
                return tokens.remove(token) != null;
            }

            @Override
            public Authentication get(String token) {
                return tokens.get(token);
            }

            @Override
            public void put(String token, Authentication auth) {
                tokens.put(token, auth);
            }
        };
        ServiceLocator.INSTANCE.ts.put(GOOD_TOKEN, auth);
    }

    protected AbstractAuthTest(WebAppDescriptor wad) {
        super(wad);
    }

}
