/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.store;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;
import javax.management.MBeanServer;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.management.ManagementService;
import org.apache.felix.dm.Component;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenStore;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default token store for STS.
 *
 * @author liemmn
 *
 */
@Deprecated
public class DefaultTokenStore implements TokenStore, ManagedService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultTokenStore.class);
    private static final String TOKEN_STORE_CONFIG_ERR = "Token store configuration error";

    private static final String TOKEN_CACHE_MANAGER = "org.opendaylight.aaa";
    private static final String TOKEN_CACHE = "tokens";
    private static final String EHCACHE_XML = "etc/ehcache.xml";

    static final String MAX_CACHED_MEMORY = "maxCachedTokensInMemory";
    static final String MAX_CACHED_DISK = "maxCachedTokensOnDisk";
    static final String SECS_TO_LIVE = "secondsToLive";
    static final String SECS_TO_IDLE = "secondsToIdle";

    // Defaults (needed only for non-Karaf deployments)
    protected static final Dictionary<String, String> DEFAULTS = new Hashtable<>();

    static {
        DEFAULTS.put(MAX_CACHED_MEMORY, Long.toString(10000));
        DEFAULTS.put(MAX_CACHED_DISK, Long.toString(1000000));
        DEFAULTS.put(SECS_TO_IDLE, Long.toString(3600));
        DEFAULTS.put(SECS_TO_LIVE, Long.toString(3600));
    }

    // Token cache lock
    private static final ReentrantLock CACHELOCK = new ReentrantLock();

    // Token cache
    private Cache tokens;

    // This should be a singleton
    DefaultTokenStore() {
    }

    // Called by DM when all required dependencies are satisfied.
    void init(Component component) {
        File ehcache = new File(EHCACHE_XML);
        CacheManager cm;
        if (ehcache.exists()) {
            cm = CacheManager.create(ehcache.getAbsolutePath());
            tokens = cm.getCache(TOKEN_CACHE);
            LOG.info("Initialized token store with custom cache config");
        } else {
            cm = CacheManager.getInstance();
            tokens = new Cache(new CacheConfiguration(TOKEN_CACHE, Integer.parseInt(DEFAULTS.get(MAX_CACHED_MEMORY)))
                    .maxEntriesLocalDisk(Integer.parseInt(DEFAULTS.get(MAX_CACHED_DISK)))
                    .timeToLiveSeconds(Long.parseLong(DEFAULTS.get(SECS_TO_LIVE)))
                    .timeToIdleSeconds(Long.parseLong(DEFAULTS.get(SECS_TO_IDLE))));
            cm.addCache(tokens);
            LOG.info("Initialized token store with default cache config");
        }
        cm.setName(TOKEN_CACHE_MANAGER);

        // JMX for cache management
        MBeanServer managementBeanServer = ManagementFactory.getPlatformMBeanServer();
        ManagementService.registerMBeans(cm, managementBeanServer, false, false, false, true);
    }

    // Called on shutdown
    void destroy() {
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

    @Override
    public void updated(@SuppressWarnings("rawtypes") Dictionary props) throws ConfigurationException {
        LOG.info("Updating token store configuration...");
        if (props == null) {
            // Someone deleted the configuration, use DEFAULTS
            props = DEFAULTS;
        }
        reconfig(props);
    }

    // Refresh cache configuration...
    private void reconfig(@SuppressWarnings("rawtypes") Dictionary props) throws ConfigurationException {
        CACHELOCK.lock();
        long secsToIdle = Long.parseLong(props.get(SECS_TO_IDLE).toString());
        long secsToLive = Long.parseLong(props.get(SECS_TO_LIVE).toString());
        int maxMem = Integer.parseInt(props.get(MAX_CACHED_MEMORY).toString());
        int maxDisk = Integer.parseInt(props.get(MAX_CACHED_DISK).toString());
        CacheConfiguration config = tokens.getCacheConfiguration();
        config.setTimeToIdleSeconds(secsToIdle);
        config.setTimeToLiveSeconds(secsToLive);
        config.maxEntriesLocalHeap(maxMem);
        config.maxEntriesLocalDisk(maxDisk);
        CACHELOCK.unlock();
    }
}
