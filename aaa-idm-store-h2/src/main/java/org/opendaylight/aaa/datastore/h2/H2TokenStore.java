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
import org.ehcache.Cache;
import org.ehcache.CachePersistenceException;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;
import org.opendaylight.aaa.api.Authentication;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.api.TokenStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2TokenStore implements AutoCloseable, TokenStore {

    private static final Logger LOG = LoggerFactory.getLogger(H2TokenStore.class);

    private static final String TOKEN_CACHE_MANAGER = "org.opendaylight.aaa";
    private static final String TOKEN_CACHE_FOLDER = "data/tokens-ehcache";
    private static final int MAX_CACHED_TOKENS_IN_MEMORY = 10000;
    private static final int MAX_CACHED_TOKENS_ON_DISK_MB = 100;

    private final Cache<String, Authentication> tokens;
    private final PersistentCacheManager cacheManager;
    private final long secondsToLive;

    public H2TokenStore(final long secondsToLive, final long secondsToIdle) {
        this.secondsToLive = secondsToIdle;
        final var cacheConfiguration = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(String.class, Authentication.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(MAX_CACHED_TOKENS_IN_MEMORY, EntryUnit.ENTRIES)
                    .disk(MAX_CACHED_TOKENS_ON_DISK_MB, MemoryUnit.MB, true)
            )
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(secondsToLive)))
            .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(secondsToIdle)))
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