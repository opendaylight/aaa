/*
 * Copyright © 2016 2016 Brocade Communications Systems and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.aaa.cli.api.AaaCliCommands;

public class AaaCliCommandsImpl implements AaaCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(AaaCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public AaaCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("AaaCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}