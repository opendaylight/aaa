/*
 * Copyright (c) 2015 - 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.web.env;

import java.io.File;
import java.io.FileNotFoundException;
import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Identical to <code>IniWebEnvironment</code> except the Ini is loaded from
 * <code>${KARAF_HOME}/etc/shiro.ini</code>.
 *
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 *
 */
public class KarafIniWebEnvironment extends IniWebEnvironment {

    private static final Logger LOG = LoggerFactory.getLogger(KarafIniWebEnvironment.class);

    /**
     * The location of shiro.ini relative to ${karaf.home}
     */
    static final String DEFAULT_SHIRO_INI_FILE = "etc" + File.separator + "shiro.ini";

    /**
     * The Shiro-specific prefix used to indicate file based Shiro configuration
     */
    static final String SHIRO_FILE_PREFIX = "file:" + File.separator;

    public KarafIniWebEnvironment() {
        LOG.info("Initializing the Web Environment using {}",
                KarafIniWebEnvironment.class.getName());
    }

    @Override
    public void init() {
        // Initialize the Shiro environment from etc/shiro.ini then delegate to
        // the parent class
        final Ini ini;
        try {
            ini = createDefaultShiroIni();
            setIni(ini);
        } catch (final FileNotFoundException e) {
            LOG.error("Could not find etc/shiro.ini", e);
        }
        super.init();
    }

    /**
     *
     * @return Ini associated with <code>$KARAF_HOME/etc/shiro.ini</code>
     * @throws FileNotFoundException
     */
    static Ini createDefaultShiroIni() throws FileNotFoundException {
        return createShiroIni(DEFAULT_SHIRO_INI_FILE);
    }

    /**
     *
     * @param path
     *            the file path, which is either absolute or relative to
     *            <code>$KARAF_HOME</code>
     * @return Ini loaded from <code>path</code>
     */
    static Ini createShiroIni(final String path) throws FileNotFoundException {
        final File f = new File(path);
        final Ini ini = new Ini();
        final String fileBasedIniPath = createFileBasedIniPath(f.getAbsolutePath());
        LOG.debug("Attempting an ini load from the file: \"{}\"", fileBasedIniPath);
        ini.loadFromPath(fileBasedIniPath);
        return ini;
    }

    /**
     *
     * @param path
     *            the file path, which is either absolute or relative to
     *            <code>$KARAF_HOME</code>
     * @return <code>file:/$KARAF_HOME/etc/shiro.ini</code>
     */
    static String createFileBasedIniPath(final String path) {
        return SHIRO_FILE_PREFIX + path;
    }
}
