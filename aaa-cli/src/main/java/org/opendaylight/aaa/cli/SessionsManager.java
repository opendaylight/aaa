/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
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
public class SessionsManager implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SessionsManager.class);

    private static SessionsManager sessionMgr = null;
    private Cache authUsers;
    private static final int MAX_CACHED_USERS_IN_MEMORY = 1;
    private static final int MAX_CACHED_USERS_ON_DISK = 1;
    private static final long SECONDS_TO_LIVE = 120;
    private static final long SECONDS_TO_IDLE = 60;
    private static final String CLI_CACHE_MANAGER = "org.opendaylight.aaa.cli";
    private static final String CLI_CACHE = "users";

    private SessionsManager() {
        CacheManager cm = CacheManager.newInstance();
        authUsers = new Cache(new CacheConfiguration(CLI_CACHE, MAX_CACHED_USERS_IN_MEMORY)
                .maxEntriesLocalDisk(MAX_CACHED_USERS_ON_DISK).timeToLiveSeconds(SECONDS_TO_LIVE)
                .timeToIdleSeconds(SECONDS_TO_IDLE));
        cm.addCache(authUsers);
        cm.setName(CLI_CACHE_MANAGER);
        LOG.info("Initialized cli authorized users cache manager");
    }

    public static SessionsManager getInstance() {
        if (sessionMgr == null) {
            sessionMgr = new SessionsManager();
        }
        return sessionMgr;
    }

    @Override
    public void close() throws Exception {
        LOG.info("Shutting down cli authorized users cache manager");
        CacheManager.getInstance().shutdown();
    }

    public void addUserSession(String userName, User usr) {
        authUsers.put(new Element(userName, usr));
    }

    public User getCurrentUser(String userName) {
        Element elem = authUsers.get(userName);
        if (elem != null) {
            return (User) elem.getObjectValue();
        }
        return null;
    }
}
