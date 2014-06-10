/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api;

import java.util.Map;

/**
 * An interface for in-bound claim transformation.
 *
 * @author liemmn
 *
 */
public interface ClaimAuth {

    /**
     * Transform a map of opaque in-bound claims into a {@link Claim} object. An
     * example of an opaque claim map entry is
     * <code>"USER_NAME" -> "joe".</code>
     * <p>
     * In-bound claims are extracted from HttpServletRequest attributes and
     * headers.
     *
     * @param claim
     *            opaque claim
     * @return normalized claim, or null if not applicable
     */
    Claim transform(Map<String, Object> claim) throws AuthenticationException;
}
