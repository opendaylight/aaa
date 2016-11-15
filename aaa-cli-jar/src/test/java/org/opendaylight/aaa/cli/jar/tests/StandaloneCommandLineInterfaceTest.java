/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.jar.tests;

import org.junit.Test;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.h2.persistence.H2Store;

/**
 * Test for the standalone (JAR'd) CLI for AAA.
 *
 * @author Michael Vorburger
 */
public class StandaloneCommandLineInterfaceTest {

    @Test
    public void test() throws Exception {
    	H2Store h2Store = new H2Store();
    	H2Store.getConfig().log();

        IIDMStore identityStore = h2Store;
        for (User user: identityStore.getUsers().getUsers()) {
            System.out.print(user.getName());
        }
    }

}
