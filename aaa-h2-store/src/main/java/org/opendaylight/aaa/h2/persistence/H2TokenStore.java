/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.h2.persistence;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mserngawy
 *
 */
public class H2TokenStore implements AutoCloseable, TokenStore {

    private static final Logger LOG = LoggerFactory.getLogger(H2TokenStore.class);
    private static final ReentrantLock cacheLock = new ReentrantLock();

    private final String TOKEN_CACHE_MANAGER = "org.opendaylight.aaa";
    private final String TOKEN_CACHE = "tokens";
    private final String MAX_CACHED_MEMORY = "maxCachedTokensInMemory";
    private final String MAX_CACHED_DISK = "maxCachedTokensOnDisk";
    private final String SECS_TO_IDLE = "secondsToIdle";
    //Public for test purpose.
    public final String SECS_TO_LIVE = "secondsToLive";


    private int maxCachedTokensInMemory = 10000;
    private int maxCachedTokensOnDisk = 100000;
    private long secondsToLive = 3600;
    private long secondsToIdle = 3600;
    private final Cache tokens;

    public H2TokenStore() {
        CacheManager cm = CacheManager.newInstance();
        tokens = new Cache( new CacheConfiguration(TOKEN_CACHE, maxCachedTokensInMemory)
                                    .maxEntriesLocalDisk(maxCachedTokensOnDisk)
                                    .timeToLiveSeconds(secondsToLive)
                                    .timeToIdleSeconds(secondsToIdle));
        cm.addCache(tokens);
        cm.setName(TOKEN_CACHE_MANAGER);
        LOG.info("Initialized token store with default cache config");
    }

    @Override
    public void close() throws Exception {
        LOG.info("Shutting down token store...");
        CacheManager.getInstance().shutdown();
    }

    @Override
    public Authentication get(String token) {
        Element elem = tokens.get(token);
        return (Authentication) (elem != null ? elem.getObjectValue() : null);
    }

    @Override
    public void put(String token, Authentication auth) {
        tokens.put(new Element(token, auth));
    }

    @Override
    public boolean delete(String token) {
        return tokens.remove(token);
    }

    @Override
    public long tokenExpiration() {
        return tokens.getCacheConfiguration().getTimeToLiveSeconds();
    }

    public void updateConfigParameter(@Nullable Map<String, Object> configParameters) {
        if (configParameters != null && !configParameters.isEmpty()) {
            LOG.debug("Tokens Config parameters received : {}", configParameters.entrySet());
            try {
                for (Map.Entry<String, Object> paramEntry : configParameters.entrySet()) {
                    if (paramEntry.getKey().equalsIgnoreCase(MAX_CACHED_MEMORY)) {
                        maxCachedTokensInMemory = Integer.parseInt((String)paramEntry.getValue());
                    }
                    else if (paramEntry.getKey().equalsIgnoreCase(MAX_CACHED_DISK)) {
                        maxCachedTokensOnDisk = Integer.parseInt((String)paramEntry.getValue());
                    }
                    else if (paramEntry.getKey().equalsIgnoreCase(SECS_TO_LIVE)) {
                        secondsToLive = Long.parseLong((String)paramEntry.getValue());
                    }
                    else if (paramEntry.getKey().equalsIgnoreCase(SECS_TO_IDLE)) {
                        secondsToIdle = Long.parseLong((String)paramEntry.getValue());
                    }
                }
                cacheLock.lock();
                CacheConfiguration config = tokens.getCacheConfiguration();
                config.setTimeToIdleSeconds(secondsToIdle);
                config.setTimeToLiveSeconds(secondsToLive);
                config.maxEntriesLocalHeap(maxCachedTokensInMemory);
                config.maxEntriesLocalDisk(maxCachedTokensOnDisk);
            } catch(Exception e) {
                LOG.error("Token store configuration error ", e);
            } finally {
                cacheLock.unlock();
            }
        }
    }

}
