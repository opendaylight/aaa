/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.filterchain.configuration;

/**
 * Responsible for parsing configuration from a configuration admin file (or
 * service). This interface may be configured through the Karaf webconsole, or
 * though adding a configuration file as follows. The configuration can be
 * specified through performing the following:
 *
 * <p>
 * Edit <code>etc/org.opendaylight.aaa.filterchain.cfg</code>:
 *
 * <p>
 * Add Filters in the following form:
 * <code>customFilterList = c.b.a.TestFilter1,f.d.e.TestFilter2,j.h.i.FilterN</code>
 *
 * <p>
 * If you wish to specify key/value init-params (normally done in web.xml), you
 * can do so by adding them scoped to the filter on a new line. For example:
 * <code>c.b.a.TestFilter1.propertyKey=propertyValue</code>
 *
 * <p>
 * This class follows the Singleton design pattern, and the instance is
 * extracted through:
 * <code>CustomFilterAdapterConfiguration.getInstance()</code>
 *
 * <p>
 * Users can subscribe for changes via {@link #registerCustomFilterAdapterConfigurationListener}
 */
public interface CustomFilterAdapterConfiguration {

    /**
     * Register for config changes.
     *
     * @param listener a listener implementing {@link CustomFilterAdapterListener}
     */
    void registerCustomFilterAdapterConfigurationListener(CustomFilterAdapterListener listener);

}
