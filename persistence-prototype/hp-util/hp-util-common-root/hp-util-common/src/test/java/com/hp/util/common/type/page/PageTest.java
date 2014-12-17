/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;
import com.hp.util.test.ThrowableTester;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class PageTest {

    @Test
    public void testConstruction() {
        Page<PageRequest, String> page = new Page<PageRequest, String>(new PageRequest(10), new ArrayList<String>());
        Assert.assertEquals(new PageRequest(10), page.getRequest());
    }

    @Test
    @SuppressWarnings("unused")
    public void testInvalidConstruction() {
        final PageRequest validPageRequest = new PageRequest(10);
        final PageRequest invalidPageRequest = null;

        final List<String> validData = new ArrayList<String>();
        final List<String> invalidData = null;

        ThrowableTester.testThrows(NullPointerException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                new Page<PageRequest, String>(invalidPageRequest, validData);
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                new Page<PageRequest, String>(validPageRequest, invalidData);
            }
        });
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<Page<?, ?>> semanticVerifier = new SemanticCompatibilityVerifier<Page<?, ?>>() {
            @Override
            public void assertSemanticCompatibility(Page<?, ?> original, Page<?, ?> replica) {
                Assert.assertEquals(original.getRequest(), replica.getRequest());
                Assert.assertEquals(original.getData(), replica.getData());
            }
        };

        List<String> data = Arrays.asList("Serializable data");
        Page<PageRequest, String> page = new Page<PageRequest, String>(new PageRequest(10), data);
        SerializabilityTester.testSerialization(page, semanticVerifier);
    }
}
