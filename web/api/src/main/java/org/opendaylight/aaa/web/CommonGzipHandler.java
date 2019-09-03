/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;

/**
 * CommonGzipHandler class stores settings for Jetty GzipHandler.
 */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE, depluralize = true)
public interface CommonGzipHandler {

    static CommonGzipHandlerBuilder builder() {
        return new CommonGzipHandlerBuilder();
    }

    ImmutableList<String> includedMimeTypes();

    ImmutableList<String> includedPaths();

}
