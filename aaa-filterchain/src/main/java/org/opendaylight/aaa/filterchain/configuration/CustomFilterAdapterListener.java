/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.filterchain.configuration;

import java.util.EventListener;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;

/**
 * React to changes to custom Filter list in config admin.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public interface CustomFilterAdapterListener extends EventListener {

    /**
     * React to configuration admin changes.
     *
     * @param injectedFilters
     *            the updated list of filters
     */
    void updateInjectedFilters(List<Filter> injectedFilters);

    /**
     * Extract the associated Filter Configuration.
     *
     * @return filter configuration information
     */
    FilterConfig getFilterConfig();
}
