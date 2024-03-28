/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.function.Supplier;
import org.ehcache.Cache;
import org.ehcache.CachePersistenceException;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.ExpiryPolicy;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.api.TokenStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationTokenStore implements AutoCloseable, TokenStore {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationTokenStore.class);

    private static final String TOKEN_CACHE_MANAGER = "org.opendaylight.aaa";
    private static final String TOKEN_CACHE_FOLDER = "data/tokens-ehcache";
    private static final int MAX_CACHED_TOKENS_IN_MEMORY = 10000;
    private static final int MAX_CACHED_TOKENS_ON_DISK_MB = 100;
    private static final ResourcePoolsBuilder RESOURCE_POOLS_BUILDER = ResourcePoolsBuilder.newResourcePoolsBuilder()
        .heap(MAX_CACHED_TOKENS_IN_MEMORY, EntryUnit.ENTRIES)
        .disk(MAX_CACHED_TOKENS_ON_DISK_MB, MemoryUnit.MB, true);

    private final Cache<String, Authentication> tokens;
    private final PersistentCacheManager cacheManager;
    private final long secondsToLive;

    public AuthenticationTokenStore(final long secondsToLive, final long secondsToIdle) {
        this.secondsToLive = secondsToIdle;
        final var cacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(String.class, Authentication.class, RESOURCE_POOLS_BUILDER)
            .withExpiry(createCombinedExpiryPolicy(secondsToLive, secondsToIdle))
            .build();

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerBuilder.persistence(TOKEN_CACHE_FOLDER))
            .withSerializer(Authentication.class, AuthenticationSerialization.class)
            .withCache(TOKEN_CACHE_MANAGER, cacheConfiguration)
            .build(true);
        tokens = cacheManager.getCache(TOKEN_CACHE_MANAGER, String.class, Authentication.class);
    }

    @Override
    public void destroyPersistentFiles() throws TokenStoreException {
        try {
            cacheManager.destroy();
        } catch (CachePersistenceException e) {
            throw new TokenStoreException("Failed to destroy persistent files", e);
        }
    }

    @Override
    public void close() {
        LOG.info("Shutting down token store...");
        cacheManager.close();
    }

    @Override
    public Authentication get(final String token) {
        return tokens.get(token);
    }

    @Override
    public void put(final String token, final Authentication auth) {
        tokens.put(token, auth);
    }

    @Override
    public void delete(final String token) {
        tokens.remove(token);
    }

    @Override
    public long tokenExpiration() {
        return secondsToLive;
    }

    private static ExpiryPolicy<String, Authentication> createCombinedExpiryPolicy(final long ttl, final long tti) {
        final var durationTtl = Duration.ofSeconds(ttl);
        final var durationTti = Duration.ofSeconds(tti);
        return new ExpiryPolicy<>() {
            @Override
            public Duration getExpiryForCreation(final String key, final Authentication value) {
                return ttl > 0 ? durationTtl : durationTti;
            }

            @Override
            public Duration getExpiryForAccess(final String key, final Supplier<? extends Authentication> value) {
                return tti > 0 ? durationTti : null;
            }

            @Override
            public Duration getExpiryForUpdate(final String key, final Supplier<? extends Authentication> oldValue,
                    final Authentication newValue) {
                return tti > 0 ? durationTti : durationTtl;
            }
        };
    }

    public record AuthenticationSerialization(ClassLoader classLoader) implements Serializer<Authentication> {
        @Override
        public ByteBuffer serialize(final Authentication authentication) throws SerializerException {
            try {
                final var byteArrayOutputStream = new ByteArrayOutputStream();
                final var objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(authentication);
                objectOutputStream.flush();
                final var bytes = byteArrayOutputStream.toByteArray();
                objectOutputStream.close();
                return ByteBuffer.wrap(bytes);
            } catch (IOException e) {
                throw new SerializerException("Serialization failed", e);
            }
        }

        @Override
        public Authentication read(final ByteBuffer binary) throws ClassNotFoundException, SerializerException {
            try (var objectInputStream = new ObjectInputStream(new ByteArrayInputStream(binary.array())) {
                @Override
                protected Class<?> resolveClass(final ObjectStreamClass objectStreamClass) throws IOException,
                        ClassNotFoundException {
                    try {
                        return Class.forName(objectStreamClass.getName(), false, classLoader);
                    } catch (ClassNotFoundException ex) {
                        return super.resolveClass(objectStreamClass);
                    }
                }
            }) {
                return (Authentication) objectInputStream.readObject();
            } catch (IOException e) {
                throw new SerializerException("Error deserializing Authentication object", e);
            }
        }

        @Override
        public boolean equals(final Authentication authentication, final ByteBuffer binary)
                throws ClassNotFoundException, SerializerException {
            final var readAuthentication = read(binary);
            return authentication.equals(readAuthentication);
        }
    }

}