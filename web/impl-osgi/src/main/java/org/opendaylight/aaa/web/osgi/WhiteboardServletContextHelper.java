/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.osgi;

import org.osgi.framework.Bundle;
import org.osgi.service.http.context.ServletContextHelper;

/**
 * Custom {@link ServletContextHelper} for use as the top encapsulating object.
 */
final class WhiteboardServletContextHelper extends ServletContextHelper {
    WhiteboardServletContextHelper(final Bundle bundle) {
        super(bundle);
    }
}
