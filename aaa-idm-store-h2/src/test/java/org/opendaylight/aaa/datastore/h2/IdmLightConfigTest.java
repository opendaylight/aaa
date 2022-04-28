/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for IdmLightConfig.
 *
 * @author Michael Vorburger
 */
public class IdmLightConfigTest {
    @Test
    public void testDefaults() {
        IdmLightConfig config = new IdmLightConfigBuilder().dbUser("foo").dbPwd("bar").build();
        assertEquals("org.h2.Driver", config.getDbDriver());
        assertEquals("jdbc:h2:./data/idmlight.db", config.getDbConnectionString());
        assertEquals("foo", config.getDbUser());
        assertEquals("bar", config.getDbPwd());
        assertEquals(3, config.getDbValidTimeOut());
    }

    @Test
    public void testCustomDirectory() {
        IdmLightConfigBuilder builder = new IdmLightConfigBuilder();
        builder.dbUser("foo").dbPwd("bar");
        builder.dbDirectory("target");
        IdmLightConfig config = builder.build();
        assertEquals("jdbc:h2:target/idmlight.db", config.getDbConnectionString());
    }

    @Test
    public void testCustomConnectionString() {
        IdmLightConfigBuilder builder = new IdmLightConfigBuilder();
        builder.dbUser("foo").dbPwd("bar");
        builder.dbConnectionString("jdbc:mysql://localhost/test");
        IdmLightConfig config = builder.build();
        assertEquals("jdbc:mysql://localhost/test", config.getDbConnectionString());
    }
}
