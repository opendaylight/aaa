/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api.tokenauthrealm.auth;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import org.junit.Test;
import org.opendaylight.aaa.api.PasswordCredentials;

public class PasswordCredentialTest {

    @Test
    public void testBuilder() {
        PasswordCredentials pc1 = new PasswordCredentialBuilder().setUserName("bob")
                .setPassword("secrete").build();
        assertEquals("bob", pc1.username());
        assertEquals("secrete", pc1.password());

        PasswordCredentials pc2 = new PasswordCredentialBuilder().setUserName("bob")
                .setPassword("secrete").build();
        assertEquals(pc1, pc2);

        PasswordCredentials pc3 = new PasswordCredentialBuilder().setUserName("bob")
                .setPassword("secret").build();
        HashSet<PasswordCredentials> pcs = new HashSet<>();
        pcs.add(pc1);
        pcs.add(pc2);
        pcs.add(pc3);
        assertEquals(2, pcs.size());
    }

}
