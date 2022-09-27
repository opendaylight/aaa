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

    public abstract List<String> urlPatterns();

    public abstract Map<String, String> initParams();

    @Default
    public Boolean getAsyncSupported() {
        return false;
    }

    @Value.Check
    protected void check() {
        urlPatterns().forEach(FilterDetails::checkUrlPattern);
    }

    private static void checkUrlPattern(final String filterPathSpec) {
        if (filterPathSpec != null && !filterPathSpec.isEmpty()) {
            int len = filterPathSpec.length();
            int idx;
            if (filterPathSpec.charAt(0) == '/') {
                if (len == 1) {
                    return;
                }

                idx = filterPathSpec.indexOf('*');
                if (idx < 0) {
                    return;
                }

                if (idx != len - 1) {
                    throw new IllegalArgumentException("Servlet Spec 12.2 violation: glob '*' can only exist at end of"
                            + "prefix based matches: bad spec \"" + filterPathSpec + "\"");
                }
            } else {
                if (!filterPathSpec.startsWith("*.")) {
                    throw new IllegalArgumentException("Servlet Spec 12.2 violation: path spec must start with \"/\" or"
                            + "\"*.\": bad spec \"" + filterPathSpec + "\"");
                }

                idx = filterPathSpec.indexOf('/');
                if (idx >= 0) {
                    throw new IllegalArgumentException("Servlet Spec 12.2 violation: suffix based path spec cannot have"
                            + "path separators: bad spec \"" + filterPathSpec + "\"");
                }

                idx = filterPathSpec.indexOf('*', 2);
                if (idx >= 1) {
                    throw new IllegalArgumentException("Servlet Spec 12.2 violation: suffix based path spec cannot have"
                            + "multiple glob '*': bad spec \"" + filterPathSpec + "\"");
                }
            }
        }
    }
}
