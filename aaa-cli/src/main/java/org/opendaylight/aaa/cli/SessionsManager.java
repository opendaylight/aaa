/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.Element;

import org.opendaylight.aaa.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mserngawy
 *
 */
public class SessionsManager implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SessionsManager.class);

    private static SessionsManager sessionMgr = null;
    private Cache authUsers;
    private final int maxCachedTokensInMemory = 10;
    private final int maxCachedTokensOnDisk = 10;
    private final long secondsToLive = 60;
    private final long secondsToIdle = 30;//3600;
    private final String CLI_CACHE_MANAGER = "org.opendaylight.aaa.cli";
    private final String CLI_CACHE = "users";

    public static SessionsManager getInstance() {
        if (sessionMgr == null) {
            sessionMgr = new SessionsManager();
        }
        return sessionMgr;
    }

    private SessionsManager() {
        CacheManager cm = CacheManager.newInstance();
        authUsers = new Cache( new CacheConfiguration(CLI_CACHE, maxCachedTokensInMemory)
                                    .maxEntriesLocalDisk(maxCachedTokensOnDisk)
                                    .timeToLiveSeconds(secondsToLive)
                                    .timeToIdleSeconds(secondsToIdle));
        cm.addCache(authUsers);
        cm.setName(CLI_CACHE_MANAGER);
        LOG.info("Initialized cli authorized users cach manager");
    }

    @Override
    public void close() throws Exception {
        LOG.info("Shutting down cli authorized users cach manager");
        CacheManager.getInstance().shutdown();
    }

    public void addUserSession(String userName, User usr) {
        authUsers.put(new Element(userName, usr));
    }

    public User getCurrentUser(String userName) {
        try {
            Element elem = authUsers.get(userName);
            if (elem != null) {
                return (User) elem.getObjectValue();
            }
        } catch (Exception e){
            LOG.debug("Error while getting userName {} ", userName, e);
        }
        return null;
    }
}
