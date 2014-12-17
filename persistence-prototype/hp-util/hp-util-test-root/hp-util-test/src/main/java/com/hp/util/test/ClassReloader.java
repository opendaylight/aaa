/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Class loader able to reload classes using a different jar file.
 * 
 * @author Fabiel Zuniga
 */
final class ClassReloader extends ClassLoader {
    
    /*
     * <p>
     * Example taken from:
     * http://tutorials.jenkov.com/java-reflection/dynamic-class-loading-reloading.html
     * <p>
     * Classes that exist in parent class loaders are used. So if you want two versions of a class
     * those classes must not be there.
     * <p>
     * Dynamic class reloading is challenging. Java's built-in Class loaders always checks if a
     * class is already loaded before loading it. Reloading the class is therefore not possible
     * using Java's built-in class loaders. To reload a class you will have to implement your own
     * ClassLoader subclass.
     * <p>
     * The following is an example to load two different versions of a class where none is part of
     * the default classloader's class path:
     *
     * <pre>
     * common.jar
     * BaseInterface
     *
     * v1.jar
     * Hello implements BaseInterface
     *
     * v2.jar
     * Hello implements BaseInterface
     *
     * Program:
     * loader1 = new URLClassLoader(new URL[] {new File("v1.jar").toURL()}, Thread.currentThread().getContextClassLoader());
     * loader2 = new URLClassLoader(new URL[] {new File("v2.jar").toURL()}, Thread.currentThread().getContextClassLoader());
     * Class<?> c1 = loader1.loadClass("com.abc.Hello");
     * Class<?> c2 = loader1.loadClass("com.abc.Hello");
     * BaseInterface i1 = (BaseInterface) c1.newInstance();
     * BaseInterface i2 = (BaseInterface) c2.newInstance();
     * </pre>
     */
    
    private Path classpath;
    private List<String> classesToReload;

    /**
     * Reloads classes even though they have already been loaded by the parent class loader.
     *
     * @param classpath class path to the jar file used to reload classes from
     * @param parentClassLoader parent class loader used to load any other class
     * @param classesToReload classes to reload using this class loader. Any other class will be
     *            loaded using {@code parentClassLoader}. For example, if a class to reload
     *            implements {@link Serializable}, this class loader will try to first load it,
     *            however if {@link Serializable} is not part of {@code classesToReload} it will
     *            be loaded using the parentClassLoader.
     */
    public ClassReloader(Path classpath, ClassLoader parentClassLoader, String... classesToReload) {
        super(parentClassLoader);
        if (classpath == null) {
            throw new NullPointerException("classpath cannot be null");
        }

        if (!classpath.toString().toLowerCase().endsWith(".jar")) {
            throw new IllegalArgumentException("classpath must be a jar file");
        }

        if (parentClassLoader == null) {
            throw new NullPointerException("parentClassLoader cannot be null");
        }

        if(classesToReload.length == 0) {
            throw new IllegalArgumentException("If classesToReload is empty this classloader is just a regular classloader");
        }

        this.classpath = classpath;
        this.classesToReload = Arrays.asList(classesToReload);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (!this.classesToReload.contains(name)) {
            return super.loadClass(name);
        }

        try {
            String fileName = getFileName(name);
            try (JarFile jarFile = new JarFile(this.classpath.toAbsolutePath().toString())) {
                Enumeration<JarEntry> jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry jarEntry = jarEntries.nextElement();
                    if (jarEntry.getName().equals(fileName)) {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        try (InputStream input = jarFile.getInputStream(jarEntry)) {
                            int data = input.read();
                            while (data != -1) {
                                buffer.write(data);
                                data = input.read();
                            }
                        }
                        byte[] classData = buffer.toByteArray();
                        return defineClass(name, classData, 0, classData.length);
                    }
                }
            }
            throw new ClassNotFoundException("Class not found: " + name + " in path " + this.classpath);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFileName(String className) {
        return className.replace(".", "/") + ".class";
    }
}