/*
 * Copyright © 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.impl.password.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apache.shiro.codec.Base64;
import org.junit.Test;
import org.opendaylight.aaa.api.password.service.PasswordHash;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.impl.password.service.DefaultPasswordHashService.Configuration;

public class DefaultPasswordHashServiceTest {

    @Test
    public void testDefault() {
        PasswordHashService hashService = new DefaultPasswordHashService();
        PasswordHash hash = hashService.getPasswordHash("password");
        assertEquals(DefaultPasswordHashService.DEFAULT_HASH_ALGORITHM, hash.getAlgorithmName());
        assertEquals(DefaultPasswordHashService.DEFAULT_NUM_ITERATIONS, hash.getIterations());
        assertNotNull(hash.getSalt());
        assertNotNull(hash.getHashedPassword());


        assertEquals(hash.getSalt(),
                hashService.getPasswordHash("password", hash.getSalt()).getSalt());
        // make sure that when utilizing the returned salt, the answer is the same
        assertEquals(hash.getHashedPassword(),
                hashService.getPasswordHash("password", hash.getSalt()).getHashedPassword());
    }

    @Test
    public void testWithSpecialConfiguration() {
        String privateSalt = Base64.encodeToString("somePrivateSalt".getBytes());
        String publicSalt = Base64.encodeToString("somePublicSalt".getBytes());

        Configuration config = mock(Configuration.class);
        doReturn("MD5").when(config).algorithm();
        doReturn(24).when(config).iterations();
        doReturn("privateSalt").when(config).privateSalt();

        DefaultPasswordHashService hashService = new DefaultPasswordHashService(config);
        PasswordHash hash = hashService.getPasswordHash("password", publicSalt);
        assertEquals("MD5", hash.getAlgorithmName());
        assertEquals(24, hash.getIterations());
        assertEquals(publicSalt, hash.getSalt());
        assertNotNull(hash.getHashedPassword());
        assertEquals(hash.getHashedPassword(),
                hashService.getPasswordHash("password", hash.getSalt()).getHashedPassword());

        doReturn(20).when(config).iterations();

        hashService = new DefaultPasswordHashService(config);
        hash = hashService.getPasswordHash("password");
        assertEquals(hash.getSalt(),
                hashService.getPasswordHash("password", hash.getSalt()).getSalt());
        // make sure that when utilizing the returned salt, the answer is the same
        assertEquals(hash.getHashedPassword(),
                hashService.getPasswordHash("password", hash.getSalt()).getHashedPassword());

        assertEquals(hashService.getPasswordHash("password", privateSalt).getHashedPassword(),
                hashService.getPasswordHash("password", privateSalt).getHashedPassword());
    }

    @Test
    public void testPasswordsMatch() {
        PasswordHashService hashService = new DefaultPasswordHashService();
        PasswordHash passwordHash = hashService.getPasswordHash("password");
        assertTrue(hashService.passwordsMatch("password",
                passwordHash.getHashedPassword(), passwordHash.getSalt()));
    }
}