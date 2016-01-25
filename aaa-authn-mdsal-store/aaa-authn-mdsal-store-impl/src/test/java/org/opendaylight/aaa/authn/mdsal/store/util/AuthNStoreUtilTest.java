/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authn.mdsal.store.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.token_list.UserTokens;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.Claims;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.ClaimsBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.ClaimsKey;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class AuthNStoreUtilTest {

    private String token = "foo_token_test";
    private String userId = "123";
    private Long expire = new Long(365);
    @Mock
    private Authentication auth;
    @Mock
    private UserTokens tokens;
    @Mock
    private Claims claims;

    @Test
    public void testCreateInstIdentifierForTokencache() {
        assertTrue(AuthNStoreUtil.createInstIdentifierForTokencache("") == null);
        assertNotNull(AuthNStoreUtil.createInstIdentifierForTokencache(token));
    }

    @Test
    public void testCreateInstIdentifierUserTokens() {
        assertTrue(AuthNStoreUtil.createInstIdentifierUserTokens("", "") == null);
        assertNotNull(AuthNStoreUtil.createInstIdentifierUserTokens(userId, token));
    }

    @Test
    public void testCreateClaimsRecord() {
        assertTrue(AuthNStoreUtil.createClaimsRecord("", null) == null);
        assertNotNull(AuthNStoreUtil.createClaimsRecord(token, auth));
    }

    @Test
    public void testCreateUserTokens() {
        assertTrue(AuthNStoreUtil.createUserTokens("", null) == null);
        assertNotNull(AuthNStoreUtil.createUserTokens(token, expire));
    }

    @Test
    public void testCreateTokenList() {
        assertTrue(AuthNStoreUtil.createTokenList(null, "") == null);
        assertNotNull(AuthNStoreUtil.createTokenList(tokens, userId));
    }

    @Test
    public void testConvertClaimToAuthentication() {
        ClaimsKey claimsKey = new ClaimsKey(token);
        ClaimsBuilder claimsBuilder = new ClaimsBuilder();
        claimsBuilder.setClientId("123");
        claimsBuilder.setDomain("foo_domain");
        claimsBuilder.setKey(claimsKey);
        List<String> roles = new ArrayList<String>();
        roles.add("foo_role");
        claimsBuilder.setRoles(roles);
        claimsBuilder.setToken(token);
        claimsBuilder.setUser("foo_usr");
        claimsBuilder.setUserId(userId);
        Claims fooClaims = claimsBuilder.build();

        assertTrue(AuthNStoreUtil.convertClaimToAuthentication(null, expire) == null);
        assertNotNull(AuthNStoreUtil.convertClaimToAuthentication(fooClaims, expire));
    }

}
