/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class ClassReloaderTest {

    private static final Path DIFFERENT_VERSION_JAR_PATH = FileSystems.getDefault().getPath("src", "test", "resources",
            "portable-serialization-test", "previous-version.jar");

    @Test
    public void testClassReload() throws Exception {
        Class<?> multipleVersionClass = PortableClass.class;
        ClassLoader differentVersionLoader = new ClassReloader(DIFFERENT_VERSION_JAR_PATH, Thread.currentThread()
                .getContextClassLoader(), multipleVersionClass.getName());
        Class<?> differentVersionClass = differentVersionLoader.loadClass(multipleVersionClass.getName());
        Assert.assertFalse(multipleVersionClass.equals(differentVersionClass));

        /*
        Object original = multipleVersionClass.newInstance();
        Object differentVersion = differentVersionClass.newInstance();
        System.out.println(original);
        System.out.println(differentVersion);
        */
    }
}
