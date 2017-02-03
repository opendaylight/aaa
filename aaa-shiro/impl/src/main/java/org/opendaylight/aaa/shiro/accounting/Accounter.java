/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.accounting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accounter is a common place to output AAA messages. Use this class through
 * invoking <code>Logger.output("message")</code>.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class Accounter {

    private static final Logger LOG = LoggerFactory.getLogger(Accounter.class);

    /*
     * Essentially makes Accounter a singleton, avoiding the verbosity of
     * <code>Accounter.getInstance().output("message")</code>.
     */
    private Accounter() {
    }

    /**
     * Account for a particular <code>message</code>
     *
     * @param message A message for the aggregated AAA log.
     */
    public static void output(final String message) {
        LOG.debug(message);
    }
}
