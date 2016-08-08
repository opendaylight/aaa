/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.h2.config;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for providing configuration properties for the H2 data store
 * implementation.  IdmLightConfig only exposes properties that are allowed to
 * be configured within the scope of ODL.  Namely, the following are exposed:
 * 1) username
 * 2) password
 * 3) connectionString
 *
 * @author peter.mellquist@hp.com
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class IdmLightConfig {

    private static final Logger LOG = LoggerFactory.getLogger(IdmLightConfig.class);

    /**
     * The key used to represent h2 database password in the external configuration (<code>etc/h2.cfg</code>)
     * as well as internally in the properties map.
     */
    private static final String PASSWORD_KEY = "password";

    /**
     * The default password for the database
     */
    private static final String DEFAULT_PASSWORD = "bar";

    /**
     * The key used to represent h2 database username in the external configuration (<code>etc/h2.cfg</code>)
     * as well as internally in the properties map.
     */
    private static final String USERNAME_KEY = "username";

    /**
     * The default username for the database
     */
    private static final String DEFAULT_USERNAME = "foo";

    /**
     * The key used to represent h2 database connectionString in the external configuration (<code>etc/h2.cfg</code>)
     * as well as internally in the properties map.
     */
    private static final String CONNECTION_STRING_KEY = "connectionString";

    /**
     * The default connection string includes the intention to use h2 as
     * the JDBC driver, and the path for the file is located relative to
     * KARAF_HOME.
     */
    private static final String DEFAULT_CONNECTION_STRING = "jdbc:h2:./idmlight.db";

    /**
     * The configuration file is located at <code>etc/h2.cfg</code> relative to the karaf home directory.
     */
    private static final String H2_PROPERTIES_FILE = "etc" + File.separator + "h2.cfg";

    /**
     * Map of all h2 related properties that are exposed to the user for configuration.
     */
    private volatile Map<String, String> properties = new HashMap<>();

    /**
     * Creates the database configuration using properties supplied via etc/h2.cfg, or defaults
     * if no such properties exist.
     */
    public IdmLightConfig() {
        final Properties properties = getProperties();
        final ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        if (properties.isEmpty()) {
            LOG.info("No properties were found/loaded from {}", H2_PROPERTIES_FILE);
        }

        setupProperty(properties, map, USERNAME_KEY, DEFAULT_USERNAME, false);
        setupProperty(properties, map, PASSWORD_KEY, DEFAULT_PASSWORD, true);
        setupProperty(properties, map, CONNECTION_STRING_KEY, DEFAULT_CONNECTION_STRING, false);

        this.properties = map.build();
    }

    /**
     * Helper method to set up a property.  Sets the value if it exists in <code>etc/h2.cfg</code> or
     * to the default otherwise.
     *
     * @param properties read from <code>etc/h2.cfg</code>
     * @param map the partial map which is side-effected with appropriate values
     * @param key the key for the parsed property
     * @param defaultValue the default value if the parsed property is not provided
     * @param obscure set <code>true</code> to prevent secure data from being logged
     */
    private static void setupProperty(final Properties properties, final ImmutableMap.Builder<String, String> map,
                                      final String key, final String defaultValue, final boolean obscure) {

        final String extracted = properties.getProperty(key);
        final String setValue;
        boolean useDefault = false;
        if (extracted != null) {
            setValue = extracted;
        } else {
            setValue = defaultValue;
            useDefault = true;
        }
        map.put(key, setValue);
        if (!obscure) {
            LOG.info("The H2 database will use {} {}={}", (useDefault ? "default" : "extracted"), key, setValue);
        } else {
            LOG.info("The H2 database will use {} value for {}", (useDefault ? "default" : "extracted"), key);
        }
    }

    /**
     * Load properties from <code>etc/h2.cfg</code>.
     *
     * @return All parsed properties, even ones that do not apply to h2 configuration.
     */
    private Properties getProperties() {
        final Properties properties = new Properties();
        final InputStream inputStream;
        try {
            final File inputFile = new File(H2_PROPERTIES_FILE);
            inputStream = new FileInputStream(inputFile);
            properties.load(inputStream);
            inputStream.close();
        } catch(final IOException e) {
            LOG.info("Unable to load properties from config path: {}", H2_PROPERTIES_FILE, e);
        }
        return properties;
    }

    public String getDbPath() {
        return properties.get(CONNECTION_STRING_KEY);
    }

    public String getDbUser() {
        return properties.get(USERNAME_KEY);
    }

    public String getDbPwd() {
        return properties.get(PASSWORD_KEY);
    }

}
