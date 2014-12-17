/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Object input stream that loads classes using a custom class loader.
 * 
 * @author Fabiel Zuniga
 */
final class CustomClassLoaderObjectInputStream extends ObjectInputStream {

    private ClassLoader classLoader;

    CustomClassLoaderObjectInputStream(InputStream in, ClassLoader classLoader) throws IOException {
        super(in);
        this.classLoader = classLoader;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {

        try {
            String name = desc.getName();
            return Class.forName(name, false, this.classLoader);
        }
        catch (ClassNotFoundException e) {
            return super.resolveClass(desc);
        }
    }
}