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
import javax.servlet.Filter;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

/**
 * Details about a {@link Filter}.
 *
 * @author Michael Vorburger.ch
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE, depluralize = true)
public abstract class FilterDetails {

    public static FilterDetailsBuilder builder() {
        return new FilterDetailsBuilder();
    }

    public abstract Filter filter();

    @Default
    public String name() {
        return filter().getClass().getName();
    }

    /**
     * Filter url patterns.
     */
    public abstract List<String> urlPatterns();

    public abstract Map<String, String> initParams();

    @Default
    public Boolean getAsyncSupported() {
        return false;
    }
}
