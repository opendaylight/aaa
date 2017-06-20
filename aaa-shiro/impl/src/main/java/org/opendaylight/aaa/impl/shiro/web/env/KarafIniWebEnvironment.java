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
import java.util.List;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;
import org.apache.shiro.web.env.IniWebEnvironment;
import org.opendaylight.aaa.impl.AAAShiroProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.shiro.config.rev170619.ShiroConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.shiro.config.rev170619.shiro.configuration.Main;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.shiro.config.rev170619.shiro.configuration.Urls;
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

    private static final String MAIN_SECTION_HEADER = "main";
    private static final String URLS_SECTION_HEADER = "urls";

    public KarafIniWebEnvironment() {
        LOG.info("Initializing the Web Environment using {}",
                KarafIniWebEnvironment.class.getName());
    }

    private static Ini createIniFromClusteredAppConfig() {
        final Ini ini = new Ini();

        final Ini.Section mainSection = ini.addSection(MAIN_SECTION_HEADER);
        final ShiroConfiguration shiroConfiguration = AAAShiroProvider.getInstance().getShiroConfiguration();
        final List<Main> mains = shiroConfiguration.getMain();
        for (final Main main : mains) {
            mainSection.put(main.getK(), main.getV());
        }

        final Ini.Section urlsSection = ini.addSection(URLS_SECTION_HEADER);
        final List<Urls> urls = shiroConfiguration.getUrls();
        for (final Urls url : urls) {
            urlsSection.put(url.getK(), url.getV());
        }

        final Factory<SecurityManager> factory = new IniSecurityManagerFactory(ini);
        final SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);

        return ini;
    }

    @Override
    public void init() {
        // Initialize the Shiro environment from clustered-app-config
        final Ini ini;//createIniFromClusteredAppConfig();
        try {
            ini = createDefaultShiroIni();
            setIni(ini);
            super.init();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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

        final Factory<SecurityManager> factory = new IniSecurityManagerFactory(ini);
        final SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);

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
