/*
 * Copyright (c) 2015 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.web.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Ryan Goulding (ryandgoulding@gmail.com)
 */
public class KarafIniWebEnvironmentTest {
    private static File iniFile;

    @BeforeClass
    public static void setup() throws IOException {
        iniFile = createShiroIniFile();
        assertTrue(iniFile.exists());
    }

    @AfterClass
    public static void teardown() {
        iniFile.delete();
    }

    private static String createFakeShiroIniContents() {
        return "[users]\n" + "admin=admin, ROLE_ADMIN \n" + "[roles]\n" + "ROLE_ADMIN = *\n"
                + "[urls]\n" + "/** = authcBasic";
    }

    private static File createShiroIniFile() throws IOException {
        File shiroIni = File.createTempFile("shiro", "ini");
        FileWriter writer = new FileWriter(shiroIni);
        writer.write(createFakeShiroIniContents());
        writer.flush();
        writer.close();
        return shiroIni;
    }

    @Test
    public void testCreateShiroIni() throws IOException {
        Ini ini = KarafIniWebEnvironment.createShiroIni(iniFile.getAbsolutePath());
        assertNotNull(ini);
        assertNotNull(ini.getSection("users"));
        assertNotNull(ini.getSection("roles"));
        assertNotNull(ini.getSection("urls"));
        Section usersSection = ini.getSection("users");
        assertTrue(usersSection.containsKey("admin"));
        assertTrue(usersSection.get("admin").contains("admin"));
        assertTrue(usersSection.get("admin").contains("ROLE_ADMIN"));
    }

    @Test
    public void testCreateFileBasedIniPath() {
        String testPath = "/shiro.ini";
        String expectedFileBasedIniPath = KarafIniWebEnvironment.SHIRO_FILE_PREFIX + testPath;
        String actualFileBasedIniPath = KarafIniWebEnvironment.createFileBasedIniPath(testPath);
        assertEquals(expectedFileBasedIniPath, actualFileBasedIniPath);
    }

}
