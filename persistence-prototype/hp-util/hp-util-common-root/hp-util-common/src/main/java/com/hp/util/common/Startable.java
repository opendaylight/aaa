/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * Startable.
 * <p>
 * <Strong>Note:</Strong> Application-specific service interfaces should not extend from this
 * interface; see {@link Restrictable} for an example of how to restrict functionality.
 * 
 * @author Fabiel Zuniga
 */
public interface Startable {

    /**
     * Starts operations.
     * 
     * @throws Exception if errors occur while starting
     */
    public void start() throws Exception;
}
