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
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.common.type.page.MarkPageRequest.Navigation;
import com.hp.util.test.SerializabilityTester;
import com.hp.util.test.SerializabilityTester.SemanticCompatibilityVerifier;
import com.hp.util.test.ThrowableTester;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class MarkPageTest {

    @Test
    public void testConstruction() {
        MarkPage<String> page = new MarkPage<String>(new MarkPageRequest<String>(10), new ArrayList<String>());
        Assert.assertEquals(new MarkPageRequest<String>(10), page.getRequest());
    }

    @Test
    @SuppressWarnings("unused")
    public void testInvalidConstruction() {
        final MarkPageRequest<String> validRequest = new MarkPageRequest<String>(10);
        final MarkPageRequest<String> invalidRequest = null;

        final List<String> validData = new ArrayList<String>();
        final List<String> invalidData = null;

        ThrowableTester.testThrows(NullPointerException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                new MarkPage<String>(invalidRequest, validData);
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                new MarkPage<String>(validRequest, invalidData);
            }
        });
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<MarkPage<?>> semanticVerifier = new SemanticCompatibilityVerifier<MarkPage<?>>() {
            @Override
            public void assertSemanticCompatibility(MarkPage<?> original, MarkPage<?> replica) {
                Assert.assertEquals(original.getRequest(), replica.getRequest());
                Assert.assertEquals(original.getData(), replica.getData());
            }
        };

        List<String> data = Arrays.asList("Serializable data");
        MarkPage<String> page = new MarkPage<String>(new MarkPageRequest<String>(10), data);
        SerializabilityTester.testSerialization(page, semanticVerifier);
    }

    @Test
    public void testNext() {
        List<String> data = Collections.emptyList();
        MarkPage<String> page = new MarkPage<String>(new MarkPageRequest<String>(10), data);
        Assert.assertNull(page.getNextPageRequest());

        data = Arrays.asList("Element 1", "Element 2", "Element 3");
        page = new MarkPage<String>(new MarkPageRequest<String>("Element 1", Navigation.NEXT, 3), data);
        Assert.assertEquals(new MarkPageRequest<String>("Element 3", Navigation.NEXT, 3), page.getNextPageRequest());
    }

    @Test
    public void testPrevious() {
        List<String> data = Collections.emptyList();
        MarkPage<String> page = new MarkPage<String>(new MarkPageRequest<String>(10), data);
        Assert.assertNull(page.getPreviousPageRequest());

        data = Arrays.asList("Element 4", "Element 5", "Element 6");
        page = new MarkPage<String>(new MarkPageRequest<String>("Element 3", Navigation.NEXT, 3), data);
        Assert.assertEquals(new MarkPageRequest<String>("Element 4", Navigation.PREVIOUS, 3),
            page.getPreviousPageRequest());
    }
}
