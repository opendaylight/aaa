/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authn.mdsal.store.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.aaa.AuthenticationBuilder;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.TokenCacheTimes;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.Tokencache;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.TokenList;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.TokenListBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.TokenListKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.token_list.UserTokens;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.token_list.UserTokensBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.token_list.UserTokensKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.Claims;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.ClaimsBuilder;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.ClaimsKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AuthNStoreUtil {

    public static InstanceIdentifier<Claims> createInstIdentifierForTokencache(String token) {
        if (token == null || token.length() == 0)
            return null;

        InstanceIdentifier<Claims> claims_iid = InstanceIdentifier.builder(Tokencache.class)
                                                                  .child(Claims.class,
                                                                          new ClaimsKey(token))
                                                                  .build();
        return claims_iid;
    }

    public static InstanceIdentifier<UserTokens> createInstIdentifierUserTokens(String userId,
            String token) {
        if (userId == null || userId.length() == 0 || token == null || token.length() == 0)
            return null;

        InstanceIdentifier<UserTokens> userTokens_iid = InstanceIdentifier.builder(
                TokenCacheTimes.class)
                                                                          .child(TokenList.class,
                                                                                  new TokenListKey(
                                                                                          userId))
                                                                          .child(UserTokens.class,
                                                                                  new UserTokensKey(
                                                                                          token))
                                                                          .build();
        return userTokens_iid;
    }

    public static Claims createClaimsRecord(String token, Authentication auth) {
        if (auth == null || token == null || token.length() == 0)
            return null;

        ClaimsKey claimsKey = new ClaimsKey(token);
        ClaimsBuilder claimsBuilder = new ClaimsBuilder();
        claimsBuilder.setClientId(auth.clientId());
        claimsBuilder.setDomain(auth.domain());
        claimsBuilder.setKey(claimsKey);
        List<String> roles = new ArrayList<String>();
        roles.addAll(auth.roles());
        claimsBuilder.setRoles(roles);
        claimsBuilder.setToken(token);
        claimsBuilder.setUser(auth.user());
        claimsBuilder.setUserId(auth.userId());
        return claimsBuilder.build();
    }

    public static UserTokens createUserTokens(String token, Long expiration) {
        if (expiration == null || token == null || token.length() == 0)
            return null;

        UserTokensBuilder userTokensBuilder = new UserTokensBuilder();
        userTokensBuilder.setTokenid(token);
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        userTokensBuilder.setTimestamp(timestamp);
        userTokensBuilder.setExpiration(expiration);
        userTokensBuilder.setKey(new UserTokensKey(token));
        return userTokensBuilder.build();
    }

    public static TokenList createTokenList(UserTokens tokens, String userId) {
        if (tokens == null || userId == null || userId.length() == 0)
            return null;

        TokenListBuilder tokenListBuilder = new TokenListBuilder();
        tokenListBuilder.setUserId(userId);
        tokenListBuilder.setKey(new TokenListKey(userId));
        List<UserTokens> userTokens = new ArrayList<UserTokens>();
        userTokens.add(tokens);
        tokenListBuilder.setUserTokens(userTokens);
        return tokenListBuilder.build();
    }

    public static Authentication convertClaimToAuthentication(final Claims claims, Long expiration) {
        if (claims == null)
            return null;

        Claim claim = new Claim() {
            @Override
            public String clientId() {
                return claims.getClientId();
            }

            @Override
            public String userId() {
                return claims.getUserId();
            }

            @Override
            public String user() {
                return claims.getUser();
            }

            @Override
            public String domain() {
                return claims.getDomain();
            }

            @Override
            public Set<String> roles() {
                return new HashSet<>(claims.getRoles());
            }
        };
        AuthenticationBuilder authBuilder = new AuthenticationBuilder(claim);
        authBuilder.setExpiration(expiration);
        return authBuilder.build();
    }
}
