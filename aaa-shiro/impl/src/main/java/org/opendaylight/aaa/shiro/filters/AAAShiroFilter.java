/*
 * Copyright (c) 2016 - 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.filters;

import static java.util.Objects.requireNonNull;

import javax.servlet.Filter;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default AAA JAX-RS Web {@link Filter} based on {@link AbstractShiroFilter}. It gets a constant reference
 * to a Shiro {@link WebEnvironment} at instantiation time and it during {@link #init()}.
 */
public final class AAAShiroFilter extends AbstractShiroFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroFilter.class);

    private final WebEnvironment env;

    public AAAShiroFilter(final WebEnvironment env) {
        this.env = requireNonNull(env);
        LOG.debug("Instantiated AAAShiroFilter for {}", env);
    }

    @Override
    public void init() {
        LOG.debug("Initializing AAAShiroFilter");
        setSecurityManager(env.getWebSecurityManager());

        final var resolver = env.getFilterChainResolver();
        if (resolver != null) {
            setFilterChainResolver(resolver);
        }
        LOG.debug("AAAShiroFilter initialized");
    }
}
