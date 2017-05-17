/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.store.mdsal;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.idm.store.mdsal.MdsalStoreUtil;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.aaa.idm.store.mdsal.rev170517.TokenCacheTimes;
import org.opendaylight.yang.gen.v1.urn.aaa.idm.store.mdsal.rev170517.token_cache_times.TokenList;
import org.opendaylight.yang.gen.v1.urn.aaa.idm.store.mdsal.rev170517.token_cache_times.TokenListKey;
import org.opendaylight.yang.gen.v1.urn.aaa.idm.store.mdsal.rev170517.token_cache_times.token_list.UserTokens;
import org.opendaylight.yang.gen.v1.urn.aaa.idm.store.mdsal.rev170517.tokencache.Claims;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MdsalTokenStore implements AutoCloseable, TokenStore {

    private static final Logger LOG = LoggerFactory.getLogger(MdsalTokenStore.class);
    private final DataBroker broker;
    private final long timeToLive;
    private final ExecutorService deleteExpiredTokenThread = Executors.newFixedThreadPool(1);
    private final AAAEncryptionService dataEncrypter;

    public MdsalTokenStore(final DataBroker dataBroker, final AAAEncryptionService dataEncrypter, long timeToLive) {
        this.broker = dataBroker;
        this.dataEncrypter = dataEncrypter;
        this.timeToLive = timeToLive;
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
        Claims claims = MdsalStoreUtil.createClaimsRecord(token, auth);

        // create and insert parallel struct
        UserTokens userTokens = MdsalStoreUtil.createUserTokens(token, timeToLive);
        TokenList tokenlist = MdsalStoreUtil.createTokenList(userTokens, auth.userId());

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
            authentication = MdsalStoreUtil.convertClaimToAuthentication(claims, userToken.getExpiration());
        }
        deleteExpiredTokenThread.execute(deleteOldTokens(claims));
        return authentication;
    }

    @Override
    public boolean delete(String token) {
        token = dataEncrypter.encrypt(token);
        Claims claims = readClaims(token);
        final boolean result = deleteClaims(token);
        if (result) {
            deleteUserTokenFromDS(token, claims.getUserId());
        }
        deleteExpiredTokenThread.execute(deleteOldTokens(claims));
        return false;//result;
    }

    @Override
    public long tokenExpiration() {
        return timeToLive;
    }

    private void writeClaimAndTokenToStore(final Claims claims, UserTokens usertokens, final TokenList tokenlist) {
        final InstanceIdentifier<Claims> claimsId = MdsalStoreUtil
                .createInstIdentifierForTokencache(claims.getToken());
        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, claimsId, claims, true);

        final InstanceIdentifier<UserTokens> userTokensId = MdsalStoreUtil
                .createInstIdentifierUserTokens(tokenlist.getUserId(), usertokens.getTokenid());
        tx.put(LogicalDatastoreType.OPERATIONAL, userTokensId, usertokens, true);

        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = tx.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                LOG.trace("Token {} was written to datastore.", claims.getToken());
                LOG.trace("Tokenlist for userId {} was written to datastore.", tokenlist.getUserId());
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.error("Inserting token {} to datastore failed.", claims.getToken());
                LOG.trace("Inserting for userId {} tokenlist to datastore failed.", tokenlist.getUserId());
            }
        });
    }

    private Claims readClaims(String token) {
        final InstanceIdentifier<Claims> claimIds = MdsalStoreUtil.createInstIdentifierForTokencache(token);
        Claims claims = null;
        ReadTransaction rt = broker.newReadOnlyTransaction();
        CheckedFuture<Optional<Claims>, ReadFailedException> claimsFuture = rt.read(LogicalDatastoreType.OPERATIONAL,
                claimIds);
        try {
            Optional<Claims> maybeClaims = claimsFuture.checkedGet();
            if (maybeClaims.isPresent()) {
                claims = maybeClaims.get();
            }
        } catch (ReadFailedException e) {
            LOG.error("Something wrong happened in DataStore. Getting Claim for token {} failed.", token, e);
        }
        return claims;
    }

    private TokenList readTokenListFromDS(String userId) {
        InstanceIdentifier<TokenList> tokenListIdentifiers = InstanceIdentifier.builder(TokenCacheTimes.class)
                .child(TokenList.class, new TokenListKey(userId)).build();
        TokenList tokenList = null;
        ReadTransaction rt = broker.newReadOnlyTransaction();
        CheckedFuture<Optional<TokenList>, ReadFailedException> userTokenListFuture = rt
                .read(LogicalDatastoreType.OPERATIONAL, tokenListIdentifiers);
        try {
            Optional<TokenList> maybeTokenList = userTokenListFuture.checkedGet();
            if (maybeTokenList.isPresent()) {
                tokenList = maybeTokenList.get();
            }
        } catch (ReadFailedException e) {
            LOG.error("Something wrong happened in DataStore. Getting TokenList for userId {} failed.", userId, e);
        }
        return tokenList;
    }

    private UserTokens readUserTokensFromDS(String token, String userId) {
        final InstanceIdentifier<UserTokens> userTokensId = MdsalStoreUtil.createInstIdentifierUserTokens(userId,
                token);
        UserTokens userTokens = null;

        ReadTransaction rt = broker.newReadOnlyTransaction();
        CheckedFuture<Optional<UserTokens>, ReadFailedException> userTokensFuture = rt
                .read(LogicalDatastoreType.OPERATIONAL, userTokensId);

        try {
            Optional<UserTokens> maybeUserTokens = userTokensFuture.checkedGet();
            if (maybeUserTokens.isPresent()) {
                userTokens = maybeUserTokens.get();
            }
        } catch (ReadFailedException e) {
            LOG.error("Something wrong happened in DataStore. Getting UserTokens for token {} failed.", token, e);
        }

        return userTokens;
    }

    private boolean deleteClaims(String token) {
        final InstanceIdentifier<Claims> claimsIds = MdsalStoreUtil.createInstIdentifierForTokencache(token);
        boolean result = false;
        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, claimsIds);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = tx.submit();

        try {
            commitFuture.checkedGet();
            result = true;
        } catch (TransactionCommitFailedException e) {
            LOG.error("Something wrong happened in DataStore. Claim " + "deletion for token {} from DataStore failed.",
                    token, e);
        }
        return result;
    }

    private void deleteUserTokenFromDS(String token, String userId) {
        final InstanceIdentifier<UserTokens> userTokensId = MdsalStoreUtil.createInstIdentifierUserTokens(userId,
                token);

        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, userTokensId);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = tx.submit();
        try {
            commitFuture.checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("Something wrong happened in DataStore. UserToken "
                    + "deletion for token {} from DataStore failed.", token, e);
        }
    }

    private Runnable deleteOldTokens(final Claims claims) {
        return () -> {
            TokenList tokenList = null;
            if (claims != null) {
                tokenList = readTokenListFromDS(claims.getUserId());
            }
            if (tokenList != null) {
                for (UserTokens currUserToken : tokenList.getUserTokens()) {
                    long diff = System.currentTimeMillis() - currUserToken.getTimestamp().longValue();
                    if (diff > currUserToken.getExpiration() && currUserToken.getExpiration() != 0) {
                        if (deleteClaims(currUserToken.getTokenid())) {
                            deleteUserTokenFromDS(currUserToken.getTokenid(), claims.getUserId());
                            LOG.trace("Expired tokens for UserId {} deleted.", claims.getUserId());
                        }
                    }
                }
            }
        };
    }
}
