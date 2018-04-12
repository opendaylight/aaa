/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.test;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class TestListener implements ServletContextListener {

    public boolean isInitialized = false;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        isInitialized = true;
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        isInitialized = false;
    }

}
