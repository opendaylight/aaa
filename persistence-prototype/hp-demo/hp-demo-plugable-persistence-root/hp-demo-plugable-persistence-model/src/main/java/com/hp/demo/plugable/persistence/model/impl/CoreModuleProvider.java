/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.impl;

import com.hp.demo.plugable.persistence.model.CoreModule;

/**
 * {@link CoreModule} provider.
 * 
 * @author Fabiel Zuniga
 */
public final class CoreModuleProvider {

    private static final CoreModule INSTANCE = new CoreModuleImpl();

    private CoreModuleProvider() {

    }

    /**
     * Gets the {@link CoreModule}.
     * 
     * @return the {@link CoreModule}
     */
    public static CoreModule getCoreModule() {
        return INSTANCE;
    }
}
