/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.jetty;

import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.opendaylight.aaa.web.CommonHandler;

/**
 * The CommonGzipHandler interface is the specific CommonHandler interface for GzipHandler.
 */
public interface CommonGzipHandler extends CommonHandler<GzipHandler> {

}
