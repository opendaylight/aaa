/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.web.jetty;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

/**
 * Implementation for GzipHandler usage.
 */
class CommonGzipHandlerImpl implements CommonGzipHandler {

    private final GzipHandler gzipHandler;

    CommonGzipHandlerImpl(@NonNull ImmutableList<String> includedMimeTypes,
                          @NonNull ImmutableList<String> includedPaths) {
        gzipHandler = new GzipHandler();
        gzipHandler.setIncludedMimeTypes(includedMimeTypes.toArray(new String[0]));
        gzipHandler.setIncludedPaths(includedPaths.toArray(new String[0]));
    }

    @Override
    public GzipHandler getHandler() {
        return gzipHandler;
    }
}
