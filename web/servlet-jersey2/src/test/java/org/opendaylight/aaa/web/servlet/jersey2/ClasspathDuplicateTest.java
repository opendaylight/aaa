/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.servlet.jersey2;

import org.junit.ClassRule;
import org.junit.Test;
import org.opendaylight.infrautils.testutils.ClasspathHellDuplicatesCheckRule;

/**
 * Test for classpath duplicates.
 *
 * @author Michael Vorburger.ch
 */
public class ClasspathDuplicateTest {

    public static @ClassRule ClasspathHellDuplicatesCheckRule jHades = new ClasspathHellDuplicatesCheckRule();

    @Test
    public void testClasspathDuplicate() { }
}
