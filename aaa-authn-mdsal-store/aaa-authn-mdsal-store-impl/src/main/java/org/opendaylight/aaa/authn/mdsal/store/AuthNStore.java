package org.opendaylight.aaa.authn.mdsal.store;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.authn.mdsal.store.util.AuthNStoreUtil;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.TokenCacheTimes;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.TokenList;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.TokenListKey;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.token_cache_times.token_list.UserTokens;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.Claims;
import org.opendaylight.yang.gen.v1.urn.aaa.yang.authn.claims.rev141029.tokencache.ClaimsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.CheckedFuture;

/**
 * Created by wdec, tkubas on 26/08/2014.
 */

public class AuthNStore implements AutoCloseable, TokenStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthNStore.class);
    private DataBroker broker;
    private static BigInteger timeToLive;
    private static Integer timeToWait;
    private final ExecutorService deleteExpiredTokenThread = Executors.newFixedThreadPool(1);

    private LoadingCache<String, UserTokenAuthentication> authenticationTokens;

    private static boolean enableEncryption = true;
    private int transactionTimeoutInSeconds = 30;

    public AuthNStore() {
        LOGGER.info("Created MD-SAL AAA Token Cache...");
    }

    public void setBroker(DataBroker dataBrokerService) {
        this.broker = dataBrokerService;
    }




    private class AuthenticationLoader extends CacheLoader<String, UserTokenAuthentication> {
        @Override
        public UserTokenAuthentication load(String token) throws Exception {
            UserTokenAuthentication auth = getInternal(token);
            if(auth == null){
                throw new Exception(String.format("Authentication data not found for token %s", token));
            }
            return auth;
        }
    }

    private class AuthenticationRemover implements RemovalListener<String, UserTokenAuthentication> {
        @Override
        public void onRemoval(RemovalNotification<String, UserTokenAuthentication> removalNotification) {
            if(RemovalCause.EXPIRED == removalNotification.getCause()){

                LOGGER.debug("Token {} expired from cache. Will attempt to cleanup tokens from data store",
                        removalNotification.getKey());

                // If any token is about to be removed due to expiry we should trigger cleanup
                String token = encryptToken(removalNotification.getKey(), true);
                Claims claims = readClaims(token);

                deleteExpiredTokenThread.execute(deleteOldTokens(claims));
            }
        }
    }

    @Override
    public void close() throws Exception {
        deleteExpiredTokenThread.shutdown();
        LOGGER.info("MD-SAL AAA Token Cache closed...");

    }

    public static final String encryptToken(String token,
            boolean checkAlreadyEncrypted) {
        if (!enableEncryption)
            return token;
        if (checkAlreadyEncrypted) {
            if (token.startsWith(DataEncrypter.ENCRYPTED_TAG)) {
                LOGGER.error("Someone is trying to hack by using an encrypted token...");
                return token;
            }
        }
        return DataEncrypter.encrypt(token);
    }

    public static Claims decryptTokenInClaims(Claims claims) {
        ClaimsBuilder cb = new ClaimsBuilder();
        cb.setClientId(claims.getClientId());
        cb.setDomain(claims.getDomain());
        cb.setKey(claims.getKey());
        cb.setRoles(claims.getRoles());
        if (enableEncryption)
            cb.setToken(DataEncrypter.decrypt(claims.getToken()));
        else
            cb.setToken(claims.getToken());
        cb.setUser(claims.getUser());
        cb.setUserId(claims.getUserId());
        return cb.build();
    }

    @Override
    public void put(String token, Authentication auth) {
        token = encryptToken(token, false);
        Claims claims = AuthNStoreUtil.createClaimsRecord(token, auth);
        // create and insert parallel struct
        UserTokens userTokens = AuthNStoreUtil.createUserTokens(token,
                timeToLive.longValue());
        TokenList tokenlist = AuthNStoreUtil.createTokenList(userTokens,
                auth.userId());
        try {
			writeClaimAndTokenToStore(claims, userTokens, tokenlist);
		} catch (IOException e) {
			LOGGER.error("Failed to write claim to token store.",e);
			return;
		}

        LOGGER.debug("Adding token {} to cache", token);
        authenticationTokens.put(token, new UserTokenAuthentication(userTokens, auth));
    }

    @Override
    public Authentication get(String token) {
        try {
            UserTokenAuthentication authentication = authenticationTokens.get(token);
            if(authentication.isExpired()){
                LOGGER.debug("Invalidating token {} from cache", token);
                // invalidate the token, this removes it from the internal cache
                authenticationTokens.invalidate(token);
                return null;
            }
            return authentication.getAuthentication();
        } catch (ExecutionException e) {
            LOGGER.error("Failed to get Authentication data", e);
        }
        return null;
    }

    private UserTokenAuthentication getInternal(String token) {
        token = encryptToken(token, true);
        UserTokenAuthentication userTokenAuthentication = null;
        Claims claims = readClaims(token);
        if (claims != null) {
            UserTokens userToken = readUserTokensFromDS(claims.getToken(),
                    claims.getUserId());
            Authentication authentication = AuthNStoreUtil.convertClaimToAuthentication(
                    claims, userToken.getExpiration());

            userTokenAuthentication = new UserTokenAuthentication(userToken, authentication);

            if(userTokenAuthentication.isExpired()) {
                LOGGER.debug("Token {} has expired. Will attempt to cleanup tokens from data store", token);
                // trigger the cleanup job to get rid of stale tokens
                deleteExpiredTokenThread.execute(deleteOldTokens(claims));
            }

        }
        return userTokenAuthentication;
    }

    @Override
    public boolean delete(String token) {
        boolean result = deleteInternal(token);
        authenticationTokens.invalidate(token);
        return result;
    }

    private boolean deleteInternal(String token) {
        token = encryptToken(token, false);
        Claims claims = readClaims(token);
        boolean result = deleteClaims(token);
        if (result) {
            deleteUserTokenFromDS(token, claims.getUserId());
        }
        return result;
    }


    @Override
    public long tokenExpiration() {
        return timeToLive.longValue();
    }

    public void setTimeToLive(BigInteger timeToLive) {
        this.timeToLive = timeToLive;

        authenticationTokens = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(tokenExpiration(), TimeUnit.MILLISECONDS)
                .removalListener(new AuthenticationRemover()).build(new AuthenticationLoader());
    }

    public void setTimeToWait(Integer timeToWait) {
        this.timeToWait = timeToWait;
    }
    
    private void writeClaimAndTokenToStore(final Claims claims,
        UserTokens usertokens, final TokenList tokenlist) throws IOException {
        final InstanceIdentifier<Claims> claims_iid = AuthNStoreUtil.createInstIdentifierForTokencache(claims.getToken());
        final InstanceIdentifier<UserTokens> userTokens_iid = AuthNStoreUtil.createInstIdentifierUserTokens(tokenlist.getUserId(), usertokens.getTokenid());

        Exception lastException = null;

        for(int i=0;i<5;i++) {
            WriteTransaction tx = broker.newWriteOnlyTransaction();
            tx.put(LogicalDatastoreType.OPERATIONAL, claims_iid, claims, true);
            tx.put(LogicalDatastoreType.OPERATIONAL, userTokens_iid, usertokens, true);
            
            CheckedFuture<Void, TransactionCommitFailedException> commitFuture = tx.submit();
            try {
                commitFuture.checkedGet(getTransactionTimeoutInSeconds(), TimeUnit.SECONDS);
                return;
            } catch (TimeoutException | TransactionCommitFailedException e) {
                LOGGER.error(String.format("AAA MDSAL Store failed to store token to the Data Broker - Retry %s", i), e);
                lastException = e;
            }
        }

        if(lastException != null) {
            throw new IOException(lastException);
        }
    }

    private Claims readClaims(String token) {
        final InstanceIdentifier<Claims> claims_iid = AuthNStoreUtil.createInstIdentifierForTokencache(token);
        Claims claims = null;

        ReadTransaction rt = broker.newReadOnlyTransaction();
        CheckedFuture<Optional<Claims>, ReadFailedException> claimsFuture = rt
                .read(LogicalDatastoreType.OPERATIONAL, claims_iid);
        try {
            Optional<Claims> maybeClaims = claimsFuture.checkedGet(timeToWait,
                    TimeUnit.SECONDS);
            if (maybeClaims.isPresent()) {
                claims = maybeClaims.get();
                claims = decryptTokenInClaims(claims);
            } else {
                LOGGER.error("Failed to get claims from data store for token {}", token);
            }
        } catch (TimeoutException e) {
            LOGGER.error(
                    "Future timed out. Getting Claim for token {} from DataStore failed.",
                    token, e);
        } catch (ReadFailedException e) {
            LOGGER.error(
                    "Something wrong happened in DataStore. Getting Claim for token {} failed.",
                    token, e);
        }
        return claims;
    }

    private TokenList readTokenListFromDS(String userId) {
        InstanceIdentifier<TokenList> tokenList_iid = InstanceIdentifier
                .builder(TokenCacheTimes.class)
                .child(TokenList.class, new TokenListKey(userId)).build();
        TokenList tokenList = null;

        ReadTransaction rt = broker.newReadOnlyTransaction();
        CheckedFuture<Optional<TokenList>, ReadFailedException> userTokenListFuture = rt
                .read(LogicalDatastoreType.OPERATIONAL, tokenList_iid);

        try {
            Optional<TokenList> maybeTokenList = userTokenListFuture
                    .checkedGet(timeToWait, TimeUnit.SECONDS);
            if (maybeTokenList.isPresent()) {
                tokenList = maybeTokenList.get();
            } else {
                LOGGER.error("Failed to token list from data store for user {}", userId);
            }
        } catch (TimeoutException e) {
            LOGGER.error(
                    "Future timed out. Getting TokenList for userId {} from DataStore failed.",
                    userId, e);
        } catch (ReadFailedException e) {
            LOGGER.error(
                    "Something wrong happened in DataStore. Getting TokenList for userId {} failed.",
                    userId, e);
        }

        return tokenList;
    }

    private UserTokens readUserTokensFromDS(String token, String userId) {
        final InstanceIdentifier<UserTokens> userTokens_iid = AuthNStoreUtil
                .createInstIdentifierUserTokens(userId, token);
        UserTokens userTokens = null;

        ReadTransaction rt = broker.newReadOnlyTransaction();
        CheckedFuture<Optional<UserTokens>, ReadFailedException> userTokensFuture = rt
                .read(LogicalDatastoreType.OPERATIONAL, userTokens_iid);

        try {
            Optional<UserTokens> maybeUserTokens = userTokensFuture.checkedGet(
                    timeToWait, TimeUnit.SECONDS);
            if (maybeUserTokens.isPresent()) {
                userTokens = maybeUserTokens.get();
            }
        } catch (TimeoutException e) {
            LOGGER.error(
                    "Future timed out. Getting UserTokens for token {} from DataStore failed.",
                    token, e);
        } catch (ReadFailedException e) {
            LOGGER.error(
                    "Something wrong happened in DataStore. Getting UserTokens for token {} failed.",
                    token, e);
        }

        return userTokens;
    }

    private boolean deleteClaims(String token) {
        final InstanceIdentifier<Claims> claims_iid = AuthNStoreUtil
                .createInstIdentifierForTokencache(token);
        boolean result = false;

        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, claims_iid);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = tx
                .submit();

        try {
            commitFuture.checkedGet(timeToWait, TimeUnit.SECONDS);
            result = true;
        } catch (TransactionCommitFailedException e) {
            LOGGER.error(
                    "Something wrong happened in DataStore. Token {} deletion from DataStore failed.",
                    token, e);
        } catch (TimeoutException e) {
            LOGGER.error(
                    "Future timed out. Token {} deletion from DataStore failed.",
                    token, e);
        }
        return result;
    }

    private void deleteUserTokenFromDS(String token, String userId) {
        final InstanceIdentifier<UserTokens> userTokens_iid = AuthNStoreUtil
                .createInstIdentifierUserTokens(userId, token);

        WriteTransaction tx = broker.newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, userTokens_iid);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = tx
                .submit();
        try {
            commitFuture.checkedGet(timeToWait, TimeUnit.SECONDS);
        } catch (TransactionCommitFailedException e) {
            LOGGER.error("Something wrong happened in DataStore. UserToken "
                    + "deletion for token {} from DataStore failed.", token, e);
        } catch (TimeoutException e) {
            LOGGER.error(
                    "Future timed out. User token deletion for token {} from DataStore failed.",
                    token, e);
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
                                deleteUserTokenFromDS(
                                        currUserToken.getTokenid(),
                                        claims.getUserId());
                                LOGGER.trace(
                                        "Expired tokens for UserId {} deleted.",
                                        claims.getUserId());
                            }
                        }
                    }
                }

            }
        };
    }


    private static class UserTokenAuthentication {
        final UserTokens userTokens;
        final Authentication authentication;
        UserTokenAuthentication(UserTokens userTokens, Authentication authentication){
            this.userTokens = userTokens;
            this.authentication = authentication;
        }

        boolean isExpired(){
            long diff = System.currentTimeMillis()
                    - userTokens.getTimestamp().longValue();
            if (diff > userTokens.getExpiration()
                    && userTokens.getExpiration() != 0) {
                return true;
            }
            return false;
        }

        Authentication getAuthentication(){
            return authentication;
        }
    }

    int getTransactionTimeoutInSeconds(){
        return transactionTimeoutInSeconds;
    }

    @VisibleForTesting
    void setTransactionTimeoutInSeconds(int transactionTimeoutInSeconds){
        this.transactionTimeoutInSeconds = transactionTimeoutInSeconds;
    }
}
