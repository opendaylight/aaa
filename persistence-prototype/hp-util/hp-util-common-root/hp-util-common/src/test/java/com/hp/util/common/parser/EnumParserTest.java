/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.parser;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.common.Parser;
import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class EnumParserTest {

    @Test
    public void testToParsable() {
        Parser<EnumTest> parser = new EnumParser<EnumTest>(EnumTest.class);
        Assert.assertEquals("ELEMENT_1", parser.toParsable(EnumTest.ELEMENT_1));
        Assert.assertNull(parser.toParsable(null));
    }

    @Test
    public void testParse() {
        final Parser<EnumTest> parser = new EnumParser<EnumTest>(EnumTest.class);
        Assert.assertEquals(EnumTest.ELEMENT_1, parser.parse("ELEMENT_1"));
        Assert.assertNull(parser.parse(null));

        ThrowableTester.testThrows(IllegalArgumentException.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                parser.parse("Non existent");
            }

        });
    }

    private static enum EnumTest {
        ELEMENT_1, ELEMTN_2
    }
}
