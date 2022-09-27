/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import java.util.List;
import java.util.Map;
import javax.servlet.Servlet;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

/**
 * Details about a {@link Servlet}.
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE, depluralize = true)
public abstract class ServletDetails {

    public static ServletDetailsBuilder builder() {
        return new ServletDetailsBuilder();
    }

    public abstract Servlet servlet();

    @Default
    public String name() {
        return servlet().getClass().getName();
    }

    public abstract List<String> urlPatterns();

    public abstract Map<String, String> initParams();

    @Default
    public Boolean getAsyncSupported() {
        return false;
    }

    @Value.Check
    protected void check() {
        urlPatterns().forEach(ServletPathSpecValidator::checkUrlPattern);
    }
}
