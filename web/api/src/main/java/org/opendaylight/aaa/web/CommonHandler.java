/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web;

/**
 * The CommonHandler interface is the base interface for handlers.
 * @param <T> type of handler
 */
public interface CommonHandler<T> {

    /**
     * Get the current handler.
     * @return a handler object
     */
    T getHandler();
}
