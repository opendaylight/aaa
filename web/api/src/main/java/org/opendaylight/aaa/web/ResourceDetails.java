/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import org.immutables.value.Value;
import org.immutables.value.Value.Default;

/**
 * Details about a resource registration.
 *
 * @author Thomas Pantelis
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE, depluralize = true)
public interface ResourceDetails {

    static ResourceDetailsBuilder builder() {
        return new ResourceDetailsBuilder();
    }

    /**
     * The base name of the resources that will be registered, typically a directory in the bundle/jar where "/"
     * is used to denote the root.
     */
    String name();

    /**
     * The name in the URI namespace to which the resources are mapped. This defaults to the {@link #name()}.
     */
    @Default default String alias() {
        return name();
    }
}
