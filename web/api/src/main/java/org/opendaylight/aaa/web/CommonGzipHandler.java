/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.web;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;

/**
 * CommonGzipHandler class stores settings for Jetty GzipHandler.
 */
public class CommonGzipHandler {

    private final ImmutableList<String> includedMimeTypes;
    private final ImmutableList<String> includedPaths;

    /**
     * Constructor creates instance to store immutable lists of included MIME types and included paths for Jetty
     * GzipHandler.
     *
     * @param includedMimeTypes included MIME types for GzipHandler
     * @param includedPaths     included paths for GzipHandler
     */
    CommonGzipHandler(@NonNull ImmutableList<String> includedMimeTypes,
                      @NonNull ImmutableList<String> includedPaths) {
        this.includedMimeTypes = includedMimeTypes;
        this.includedPaths = includedPaths;
    }

    /**
     * Get immutable list of included MIME types for Jetty GzipHandler.
     *
     * @return immutable list of included MIME types for GzipHandler
     */
    public @NonNull ImmutableList<String> getIncludedMimeTypes() {
        return includedMimeTypes;
    }

    /**
     * Get immutable list of included paths for Jetty GzipHandler.
     *
     * @return immutable list of included paths for GzipHandler
     */
    public @NonNull ImmutableList<String> getIncludedPaths() {
        return includedPaths;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        CommonGzipHandler that = (CommonGzipHandler) obj;

        if (!includedMimeTypes.equals(that.includedMimeTypes)) {
            return false;
        }
        return includedPaths.equals(that.includedPaths);
    }

    @Override
    public int hashCode() {
        int result = includedMimeTypes.hashCode();
        result = 31 * result + includedPaths.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CommonGzipHandler{" + "includedMimeTypes=" + includedMimeTypes
            + ", includedPaths=" + includedPaths + '}';
    }
}
