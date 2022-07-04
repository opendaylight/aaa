/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.osgi;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A dummy class to support HTTP whiteboard resource registration.
 */
final class WhiteboardResource {
    static final @NonNull WhiteboardResource INSTANCE = new WhiteboardResource();

    private WhiteboardResource() {
        // Hidden on purpose
    }
}
