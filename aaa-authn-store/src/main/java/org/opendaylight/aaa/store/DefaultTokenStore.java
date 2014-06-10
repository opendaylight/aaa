package org.opendaylight.aaa.store;

import java.lang.management.ManagementFactory;
import java.util.Dictionary;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.management.ManagementService;

import org.apache.felix.dm.Component;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenStore;

/**
 * A default token store for STS.
 *
 * @author liemmn
 *
 */
public class DefaultTokenStore implements TokenStore {
    private static final String TOKEN_CACHE_MANAGER = "org.opendaylight.aaa";

    private static final String TOKEN_CACHE = "tokens";

    static final String MAX_CACHED = "maxCachedTokens";
    static final String SECS_TO_LIVE = "secondsToLive";
    static final String SECS_TO_IDLE = "secondsToIdle";

    // Token cache registration lock
    private static final ReentrantLock regLock = new ReentrantLock();

    // Token cache
    private Cache tokens;

    // Called by the dependency manager when all the required dependencies are
    // satisfied.
    @SuppressWarnings("unchecked")
    void init(Component c) {
        Dictionary<String, String> props = c.getServiceProperties();

        CacheManager cm = CacheManager.getInstance();

        // In OSGi, we can have another bundle create the cache already
        regLock.lock();
        try {
            tokens = cm.getCache(TOKEN_CACHE);
            if (tokens == null) {
                cm.setName(TOKEN_CACHE_MANAGER);
                tokens = new Cache(TOKEN_CACHE, Integer.parseInt(props
                        .get(MAX_CACHED)), false, false, Integer.parseInt(props
                        .get(SECS_TO_LIVE)), Integer.parseInt(props
                        .get(SECS_TO_IDLE)));
                cm.addCache(tokens);
                // JMX for cache management
                MBeanServer mBeanServer = ManagementFactory
                        .getPlatformMBeanServer();
                ManagementService.registerMBeans(cm, mBeanServer, false, false,
                        false, true);
            }
        } finally {
            regLock.unlock();
        }
    }

    // Called on shutdown
    void destroy() {
        CacheManager.getInstance().shutdown();
    }

    @Override
    public Authentication get(String token) {
        Element elem = tokens.get(token);
        return (Authentication) ((elem != null) ? elem.getObjectValue() : null);
    }

    @Override
    public void put(String token, Authentication auth) {
        tokens.put(new Element(token, auth));
    }

    @Override
    public boolean delete(String token) {
        return tokens.remove(token);
    }

}
