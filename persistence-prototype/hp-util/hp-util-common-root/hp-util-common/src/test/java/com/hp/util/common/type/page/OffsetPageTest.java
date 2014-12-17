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
public class OffsetPageTest {

    @Test
    public void testConstruction() {
        OffsetPage<String> page = new OffsetPage<String>(new OffsetPageRequest(10), new ArrayList<String>(), 0);
        Assert.assertEquals(0, page.getTotalRecordCount());
        Assert.assertEquals(new OffsetPageRequest(10), page.getRequest());
    }

    @Test
    @SuppressWarnings("unused")
    public void testInvalidConstruction() {
        final OffsetPageRequest validRequest = new OffsetPageRequest(10);
        final OffsetPageRequest invalidRequest = null;

        final List<String> validData = new ArrayList<String>();
        final List<String> invalidData = null;

        final long validTotalRecordCount = 0;
        final long invalidTotalRecordCount = -1;

        ThrowableTester.testThrows(NullPointerException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                new OffsetPage<String>(invalidRequest, validData, validTotalRecordCount);
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                new OffsetPage<String>(validRequest, invalidData, validTotalRecordCount);
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                new OffsetPage<String>(validRequest, validData, invalidTotalRecordCount);
            }
        });
    }

    @Test
    public void testSerialization() {
        SemanticCompatibilityVerifier<OffsetPage<?>> semanticVerifier = new SemanticCompatibilityVerifier<OffsetPage<?>>() {
            @Override
            public void assertSemanticCompatibility(OffsetPage<?> original, OffsetPage<?> replica) {
                Assert.assertEquals(original.getRequest(), replica.getRequest());
                Assert.assertEquals(original.getData(), replica.getData());
                Assert.assertEquals(original.getTotalRecordCount(), replica.getTotalRecordCount());
            }
        };

        List<String> data = Arrays.asList("Serializable data");
        OffsetPage<String> page = new OffsetPage<String>(new OffsetPageRequest(10), data, 0);
        SerializabilityTester.testSerialization(page, semanticVerifier);
    }

    @Test
    public void testGetPageCount() {
        OffsetPage<String> page = new OffsetPage<String>(new OffsetPageRequest(10), new ArrayList<String>(), 0);
        Assert.assertEquals(0, page.getTotalPageCount());

        page = new OffsetPage<String>(new OffsetPageRequest(10), CreateFakeData(10), 10);
        Assert.assertEquals(1, page.getTotalPageCount());

        page = new OffsetPage<String>(new OffsetPageRequest(10), CreateFakeData(10), 11);
        Assert.assertEquals(2, page.getTotalPageCount());

        page = new OffsetPage<String>(new OffsetPageRequest(10), CreateFakeData(10), 20);
        Assert.assertEquals(2, page.getTotalPageCount());

        page = new OffsetPage<String>(new OffsetPageRequest(2, 10), CreateFakeData(10), 20);
        Assert.assertEquals(3, page.getTotalPageCount());

        page = new OffsetPage<String>(new OffsetPageRequest(100, 10), CreateFakeData(0), 20);
        Assert.assertEquals(2, page.getTotalPageCount());

        page = new OffsetPage<String>(new OffsetPageRequest(15, 10), CreateFakeData(5), 21);
        Assert.assertEquals(4, page.getTotalPageCount());

        page = new OffsetPage<String>(new OffsetPageRequest(30, 10), CreateFakeData(10), 100);
        Assert.assertEquals(10, page.getTotalPageCount());

        page = new OffsetPage<String>(new OffsetPageRequest(30, 10), CreateFakeData(0), 100);
        Assert.assertEquals(11, page.getTotalPageCount());
    }

    @Test
    public void testNext() {
        OffsetPage<String> page = new OffsetPage<String>(new OffsetPageRequest(10), CreateFakeData(10), 20);
        Assert.assertTrue(page.hasNext());
        Assert.assertEquals(new OffsetPageRequest(10, 10), page.getNextPageRequest());

        page = new OffsetPage<String>(new OffsetPageRequest(10), CreateFakeData(10), 11);
        Assert.assertTrue(page.hasNext());
        Assert.assertEquals(new OffsetPageRequest(10, 10), page.getNextPageRequest());

        page = new OffsetPage<String>(new OffsetPageRequest(10), CreateFakeData(10), 10);
        Assert.assertFalse(page.hasNext());
        Assert.assertNull(page.getNextPageRequest());

        page = new OffsetPage<String>(new OffsetPageRequest(15, 10), CreateFakeData(5), 20);
        Assert.assertFalse(page.hasNext());
        Assert.assertNull(page.getNextPageRequest());

        page = new OffsetPage<String>(new OffsetPageRequest(15, 10), CreateFakeData(5), 21);
        Assert.assertTrue(page.hasNext());
        Assert.assertEquals(new OffsetPageRequest(20, 10), page.getNextPageRequest());
    }

    @Test
    public void testPrevious() {
        OffsetPage<String> page = new OffsetPage<String>(new OffsetPageRequest(10), CreateFakeData(10), 20);
        Assert.assertFalse(page.hasPrevious());
        Assert.assertNull(page.getPreviousPageRequest());

        page = new OffsetPage<String>(new OffsetPageRequest(1, 10), CreateFakeData(10), 11);
        Assert.assertTrue(page.hasPrevious());
        Assert.assertEquals(new OffsetPageRequest(0, 10), page.getPreviousPageRequest());

        page = new OffsetPage<String>(new OffsetPageRequest(30, 10), CreateFakeData(10), 100);
        Assert.assertTrue(page.hasPrevious());
        Assert.assertEquals(new OffsetPageRequest(20, 10), page.getPreviousPageRequest());

        page = new OffsetPage<String>(new OffsetPageRequest(30, 10), CreateFakeData(5), 100);
        Assert.assertTrue(page.hasPrevious());
        Assert.assertEquals(new OffsetPageRequest(20, 10), page.getPreviousPageRequest());
    }

    private static List<String> CreateFakeData(int size) {
        List<String> data = new ArrayList<String>(size);

        for (int i = 0; i < size; i++) {
            data.add("Fake Data " + i);
        }

        return data;
    }
}
