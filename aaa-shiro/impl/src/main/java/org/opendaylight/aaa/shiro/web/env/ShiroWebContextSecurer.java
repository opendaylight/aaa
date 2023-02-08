/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import org.opendaylight.aaa.shiro.filters.AAAShiroFilter;
import org.opendaylight.aaa.web.FilterDetails;
import org.opendaylight.aaa.web.WebContext;
import org.opendaylight.aaa.web.WebContextSecurer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Secures a {@link WebContext} using Shiro.
 *
 * @author Michael Vorburger.ch
 */
@Component
public class ShiroWebContextSecurer implements WebContextSecurer {
    private final AAAShiroWebEnvironment webEnvironment;

    @Activate
    public ShiroWebContextSecurer(@Reference final AAAShiroWebEnvironment webEnvironment) {
        this.webEnvironment = requireNonNull(webEnvironment);
    }

    @Override
    public void requireAuthentication(final WebContext.Builder webContextBuilder, final boolean asyncSupported,
            final String... urlPatterns) {
        // AAA filter in front of these REST web services as well as for moon endpoints
        final var filterBuilder = FilterDetails.builder()
            .filter(new AAAShiroFilter(webEnvironment))
            .asyncSupported(asyncSupported);
        Arrays.stream(urlPatterns).forEach(filterBuilder::addUrlPattern);

        webContextBuilder.addFilter(filterBuilder.build());
    }
}
