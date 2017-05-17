/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm.store.h2;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mserngawy
 *
 */
public class H2TokenStore implements AutoCloseable, TokenStore {

    private static final Logger LOG = LoggerFactory.getLogger(H2TokenStore.class);

    private final String TOKEN_CACHE_MANAGER = "org.opendaylight.aaa";
    private final String TOKEN_CACHE = "tokens";

    private int maxCachedTokensInMemory = 10000;
    private int maxCachedTokensOnDisk = 100000;
    private final Cache tokens;

    public H2TokenStore(long secondsToLive, long secondsToIdle) {
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
}
