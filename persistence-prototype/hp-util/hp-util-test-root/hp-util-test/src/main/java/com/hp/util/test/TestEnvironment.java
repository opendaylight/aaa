/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

/**
 * Test environment utility class.
 * 
 * @author Fabiel Zuniga
 */
public final class TestEnvironment {
    private static final String OS_PROPERTY = System.getProperty("os.name").toLowerCase();

    private TestEnvironment() {

    }

    /**
     * Gets the operative system.
     * 
     * @return the operative system
     */
    public static OperativeSystem getOperativeSystem() {
        OperativeSystem operativeSystem = null;

        if (OS_PROPERTY.indexOf("nix") >= 0 || OS_PROPERTY.indexOf("nux") >= 0 || OS_PROPERTY.indexOf("aix") > 0) {
            operativeSystem = OperativeSystem.LINUX;
        }
        else if (OS_PROPERTY.indexOf("win") >= 0) {
            operativeSystem = OperativeSystem.WINDOWS;
        }
        else if (OS_PROPERTY.indexOf("mac") >= 0) {
            operativeSystem = OperativeSystem.MAC;
        }
        else if (OS_PROPERTY.indexOf("sunos") >= 0) {
            operativeSystem = OperativeSystem.SOLARIS;
        }

        return operativeSystem;
    }

    /**
     * Operative system.
     */
    public static enum OperativeSystem {
        /** Linux */
        LINUX,
        /** Windows */
        WINDOWS,
        /** Apple MAC */
        MAC,
        /** Solaris */
        SOLARIS
    }
}
