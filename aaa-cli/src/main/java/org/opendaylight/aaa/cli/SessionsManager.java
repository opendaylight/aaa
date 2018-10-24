/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli;

import javax.annotation.Nullable;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import org.opendaylight.aaa.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SessionsManager class will keep the admin user credential vaild at the
 * cache for certain time instead of required the admin user to enter the
 * username and pwd with each aaa-cli command.
 *
 * @author mserngawy
 *
 */
public final class SessionsManager implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SessionsManager.class);

    private static SessionsManager sessionMgr = new SessionsManager();
    private final Cache authUsers;
    private static final int MAX_CACHED_USERS_IN_MEMORY = 1;
    private static final int MAX_CACHED_USERS_ON_DISK = 1;
    private static final long SECONDS_TO_LIVE = 120;
    private static final long SECONDS_TO_IDLE = 60;
    private static final String CLI_CACHE_MANAGER = "org.opendaylight.aaa.cli";
    private static final String CLI_CACHE = "users";

    private SessionsManager() {
        // When we restart, the cache manager and CLI cache are already there
        CacheManager cm = CacheManager.getCacheManager(CLI_CACHE_MANAGER);
        if (cm == null) {
            Configuration configuration = ConfigurationFactory.parseConfiguration();
            configuration.setName(CLI_CACHE_MANAGER);
            cm = CacheManager.newInstance();
        }
        Cache existingCache = cm.getCache(CLI_CACHE);
        if (existingCache != null) {
            authUsers = existingCache;
        } else {
            authUsers = new Cache(new CacheConfiguration(CLI_CACHE, MAX_CACHED_USERS_IN_MEMORY)
                    .maxEntriesLocalDisk(MAX_CACHED_USERS_ON_DISK).timeToLiveSeconds(SECONDS_TO_LIVE)
                    .timeToIdleSeconds(SECONDS_TO_IDLE));
            cm.addCache(authUsers);
        }
        LOG.info("Initialized cli authorized users cache manager");
    }

    public static SessionsManager getInstance() {
        return sessionMgr;
    }

    @Override
    public void close() {
        LOG.info("Shutting down cli authorized users cache manager");
        CacheManager.getInstance().shutdown();
    }

    public void addUserSession(String userName, User usr) {
        authUsers.put(new Element(userName, usr));
    }

    /**
     * Attempt to find the {@link User} associated with the given user name in the cache.
     *
     * @param userName The string to use for cache lookup
     * @return The {@link User} associated with the given user name, if not cached return null.
     */
    public @Nullable User getCurrentUser(String userName) {
        Element elem = authUsers.get(userName);
        if (elem != null) {
            return (User) elem.getObjectValue();
        }
        return null;
    }
}
