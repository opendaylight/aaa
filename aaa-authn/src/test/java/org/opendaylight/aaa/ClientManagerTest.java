/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa;

import static org.junit.Assert.fail;

import java.util.Dictionary;
import java.util.Hashtable;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.aaa.api.AuthenticationException;
import org.osgi.service.cm.ConfigurationException;

/**
 * ClientManager test suite.
 * @author liemmn
 *
 */
public class ClientManagerTest {
    private static final ClientManager CLIENT_MANAGER = new ClientManager();

    @Before
    public void setup() throws ConfigurationException {
        CLIENT_MANAGER.init(null);
    }

    @Test
    public void testValidate() {
        CLIENT_MANAGER.validate("dlux", "secrete");
    }

    @Test(expected = AuthenticationException.class)
    public void testFailValidate() {
        CLIENT_MANAGER.validate("dlux", "what?");
    }

    @Test
    public void testUpdate() throws ConfigurationException {
        Dictionary<String, String> configs = new Hashtable<>();
        configs.put(ClientManager.CLIENTS, "aws:amazon dlux:xxx");
        CLIENT_MANAGER.updated(configs);
        CLIENT_MANAGER.validate("aws", "amazon");
        CLIENT_MANAGER.validate("dlux", "xxx");
    }

    @Test
    public void testFailUpdate() {
        Dictionary<String, String> configs = new Hashtable<>();
        configs.put(ClientManager.CLIENTS, "aws:amazon dlux");
        try {
            CLIENT_MANAGER.updated(configs);
            fail("Shoulda failed updating bad configuration");
        } catch (ConfigurationException ce) {
            // Expected
        }
        CLIENT_MANAGER.validate("dlux", "secrete");
        try {
            CLIENT_MANAGER.validate("aws", "amazon");
            fail("Shoulda failed updating bad configuration");
        } catch (AuthenticationException ae) {
            // Expected
        }
    }
}
