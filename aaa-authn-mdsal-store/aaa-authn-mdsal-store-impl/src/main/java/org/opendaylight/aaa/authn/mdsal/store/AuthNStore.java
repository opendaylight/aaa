/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.authn.mdsal.store;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.authn.mdsal.store.util.AuthNStoreUtil;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.config.aaa.authn.mdsal.store.rev141031.MdsalConfig;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.TokenCacheTimes;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.TokenList;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.TokenListKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.token_list.UserTokens;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.Claims;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthNStore implements AutoCloseable, TokenStore {

    private static final Logger LOG = LoggerFactory.getLogger(AuthNStore.class);
    private DataBroker broker;
    private BigInteger timeToLive;
    private Integer timeToWait;
    private final ExecutorService deleteExpiredTokenThread = Executors.newFixedThreadPool(1);
    private final AAAEncryptionService dataEncrypter;

    public AuthNStore(final DataBroker dataBroker, final AAAEncryptionService dataEncrypter, final MdsalConfig mdsalConfig) {
        this.broker = dataBroker;
        this.dataEncrypter = dataEncrypter;
        this.timeToLive = mdsalConfig.getTimeToLive();
        this.timeToWait = mdsalConfig.getTimeToWait();
        LOG.info("Created MD-SAL AAA Token Cache Service...");
    }

    @Override
    public void close() throws Exception {
        deleteExpiredTokenThread.shutdown();
        LOG.info("MD-SAL AAA Token Cache closed...");
    }

    @Override
    public void put(String token, Authentication auth) {
        token = dataEncrypter.encrypt(token);
        Claims claims = AuthNStoreUtil.createClaimsRecord(token, auth);

        // create and insert parallel struct
        UserTokens userTokens = AuthNStoreUtil.createUserTokens(token, timeToLive.longValue());
        TokenList tokenlist = AuthNStoreUtil.createTokenList(userTokens, auth.userId());

        writeClaimAndTokenToStore(claims, userTokens, tokenlist);
        deleteExpiredTokenThread.execute(deleteOldTokens(claims));
    }

    @Override
    public Authentication get(String token) {
        token = dataEncrypter.encrypt(token);
        Authentication authentication = null;
        Claims claims = readClaims(token);
        if (claims != null) {
            UserTokens userToken = readUserTokensFromDS(claims.getToken(), claims.getUserId());
            authentication = AuthNStoreUtil.convertClaimToAuthentication(claims,
                    userToken.getExpiration());
        }
        deleteExpiredTokenThread.execute(deleteOldTokens(claims));
        return authentication;
    }

    @Override
    public boolean delete(String token) {
        token = dataEncrypter.encrypt(token);
        boolean result = false;
        Claims claims = readClaims(token);
        result = deleteClaims(token);
        if (result) {
            deleteUserTokenFromDS(token, claims.getUserId());
        }
        deleteExpiredTokenThread.execute(deleteOldTokens(claims));
        return result;
    }

    @Override
    public long tokenExpiration() {
        return timeToLive.longValue();
    }

    public void setTimeToLive(BigInteger timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setTimeToWait(Integer timeToWait) {
        this.timeToWait = timeToWait;
    }

    private void writeClaimAndTokenToStore(final Claims claims, UserTokens usertokens,
            final TokenList tokenlist) {

        final InstanceIdentifier<Claims> claims_iid = AuthNStoreUtil.createInstIdentifierForTokencache(claims.getToken());
        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, claims_iid, claims, true);

        final InstanceIdentifier<UserTokens> userTokens_iid = AuthNStoreUtil.createInstIdentifierUserTokens(
                tokenlist.getUserId(), usertokens.getTokenid());
        tx.put(LogicalDatastoreType.OPERATIONAL, userTokens_iid, usertokens, true);

        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = tx.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                LOG.trace("Token {} was written to datastore.", claims.getToken());
                LOG.trace("Tokenlist for userId {} was written to datastore.",
                        tokenlist.getUserId());
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("Inserting token {} to datastore failed.", claims.getToken());
                LOG.trace("Inserting for userId {} tokenlist to datastore failed.",
                        tokenlist.getUserId());
            }

        });
    }

    private Claims readClaims(String token) {
        final InstanceIdentifier<Claims> claims_iid = AuthNStoreUtil.createInstIdentifierForTokencache(token);
        Claims claims = null;
        ReadTransaction rt = broker.newReadOnlyTransaction();
        CheckedFuture<Optional<Claims>, ReadFailedException> claimsFuture = rt.read(
                LogicalDatastoreType.OPERATIONAL, claims_iid);
        try {
            Optional<Claims> maybeClaims = claimsFuture.checkedGet();
            if (maybeClaims.isPresent()) {
                claims = maybeClaims.get();
            }
        } catch (ReadFailedException e) {
            LOG.error(
                    "Something wrong happened in DataStore. Getting Claim for token {} failed.",
                    token, e);
        }
        return claims;
    }

    private TokenList readTokenListFromDS(String userId) {
        InstanceIdentifier<TokenList> tokenList_iid = InstanceIdentifier.builder(
                TokenCacheTimes.class).child(TokenList.class, new TokenListKey(userId)).build();
        TokenList tokenList = null;
        ReadTransaction rt = broker.newReadOnlyTransaction();
        CheckedFuture<Optional<TokenList>, ReadFailedException> userTokenListFuture = rt.read(
                LogicalDatastoreType.OPERATIONAL, tokenList_iid);
        try {
            Optional<TokenList> maybeTokenList = userTokenListFuture.checkedGet();
            if (maybeTokenList.isPresent()) {
                tokenList = maybeTokenList.get();
            }
        } catch (ReadFailedException e) {
            LOG.error(
                    "Something wrong happened in DataStore. Getting TokenList for userId {} failed.",
                    userId, e);
        }
        return tokenList;
    }

    private UserTokens readUserTokensFromDS(String token, String userId) {
        final InstanceIdentifier<UserTokens> userTokens_iid = AuthNStoreUtil.createInstIdentifierUserTokens(
                userId, token);
        UserTokens userTokens = null;

        ReadTransaction rt = broker.newReadOnlyTransaction();
        CheckedFuture<Optional<UserTokens>, ReadFailedException> userTokensFuture = rt.read(
                LogicalDatastoreType.OPERATIONAL, userTokens_iid);

        try {
            Optional<UserTokens> maybeUserTokens = userTokensFuture.checkedGet();
            if (maybeUserTokens.isPresent()) {
                userTokens = maybeUserTokens.get();
            }
        } catch (ReadFailedException e) {
            LOG.error(
                    "Something wrong happened in DataStore. Getting UserTokens for token {} failed.",
                    token, e);
        }

        return userTokens;
    }

    private boolean deleteClaims(String token) {
        final InstanceIdentifier<Claims> claims_iid = AuthNStoreUtil.createInstIdentifierForTokencache(token);
        boolean result = false;
        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, claims_iid);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = tx.submit();

        try {
            commitFuture.checkedGet();
            result = true;
        } catch (TransactionCommitFailedException e) {
            LOG.error("Something wrong happened in DataStore. Claim "
                    + "deletion for token {} from DataStore failed.", token, e);
        }
        return result;
    }

    private void deleteUserTokenFromDS(String token, String userId) {
        final InstanceIdentifier<UserTokens> userTokens_iid = AuthNStoreUtil.createInstIdentifierUserTokens(
                userId, token);

        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, userTokens_iid);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = tx.submit();
        try {
            commitFuture.checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("Something wrong happened in DataStore. UserToken "
                    + "deletion for token {} from DataStore failed.", token, e);
        }
    }

    private Runnable deleteOldTokens(final Claims claims) {
        return new Runnable() {

            @Override
            public void run() {
                TokenList tokenList = null;
                if (claims != null) {
                    tokenList = readTokenListFromDS(claims.getUserId());
                }
                if (tokenList != null) {
                    for (UserTokens currUserToken : tokenList.getUserTokens()) {
                        long diff = System.currentTimeMillis()
                                - currUserToken.getTimestamp().longValue();
                        if (diff > currUserToken.getExpiration()
                                && currUserToken.getExpiration() != 0) {
                            if (deleteClaims(currUserToken.getTokenid())) {
                                deleteUserTokenFromDS(currUserToken.getTokenid(),
                                        claims.getUserId());
                                LOG.trace("Expired tokens for UserId {} deleted.",
                                        claims.getUserId());
                            }
                        }
                    }
                }
            }
        };
    }
}
