/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

import java.io.Closeable;

/**
 * Classes implementing this interface are able to be terminated: cease or cause to cease operation.
 * A terminated class might not able to resume operations anymore; a new instance might be needed.
 * The stop method is invoked to release resources.
 * <p>
 * There is a difference in purpose with {@link Closeable}: {@link Closeable} is used with
 * try-with-resources. A resource is an object that must be closed after the program is finished
 * with it. try-with-resources is used for small scope resources. A stoppable is normally stopped
 * when the program terminates (Like a service).
 * <p>
 * <Strong>Note:</Strong> Application-specific service interfaces should not extend from this
 * interface; see {@link Restrictable} for an example of how to restrict functionality.
 * 
 * @author Fabiel Zuniga
 */
public interface Stoppable {

    /**
     * Stops (shutdowns) operations. Resources will be released.
     * 
     * @throws Exception if errors occur while stopping
     */
    public void stop() throws Exception;
}
