/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.filterchain.configuration;

import javax.servlet.Filter;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Common constants.
 */
@NonNullByDefault
public final class CustomFilterAdapterConstants {
    /**
     * Property marking a {@link Filter} in OSGi Service Registry for inclusion in
     * {@link CustomFilterAdapterConfiguration}. This property needs to be set to {@code true}, otherwise the filter
     * will be ignored.
     */
    public static final String FILTERCHAIN_FILTER = "odl.aaa.filterchain.filter";

    private CustomFilterAdapterConstants() {
        // Hidden on purpose
    }
}
