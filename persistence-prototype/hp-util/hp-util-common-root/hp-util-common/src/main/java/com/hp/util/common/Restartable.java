/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

/**
 * Restartable.
 * <p>
 * <Strong>Note:</Strong> Application-specific service interfaces should not extend from this
 * interface; see {@link Restrictable} for an example of how to restrict functionality.
 * 
 * @author Fabiel Zuniga
 */
public interface Restartable extends Startable, Stoppable {

}
