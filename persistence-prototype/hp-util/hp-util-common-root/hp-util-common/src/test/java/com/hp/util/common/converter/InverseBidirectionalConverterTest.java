/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.converter;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.hp.util.common.BidirectionalConverter;
import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class InverseBidirectionalConverterTest {

    @Test
    public void testConvert() {
        final String source = "source";
        final Integer target = Integer.valueOf(1);
        @SuppressWarnings("unchecked")
        BidirectionalConverter<String, Integer> delegateMock = EasyMock.createMock(BidirectionalConverter.class);

        EasyMock.expect(delegateMock.restore(EasyMock.same(target))).andReturn(source);

        EasyMock.replay(delegateMock);

        BidirectionalConverter<Integer, String> inverse = InverseBidirectionalConverter.inverse(delegateMock);
        Assert.assertSame(source, inverse.convert(target));

        EasyMock.verify(delegateMock);
    }

    @Test
    public void testRestore() throws IllegalArgumentException {
        final String source = "source";
        final Integer target = Integer.valueOf(1);
        @SuppressWarnings("unchecked")
        BidirectionalConverter<String, Integer> delegateMock = EasyMock.createMock(BidirectionalConverter.class);

        EasyMock.expect(delegateMock.convert(EasyMock.same(source))).andReturn(target);

        EasyMock.replay(delegateMock);

        BidirectionalConverter<Integer, String> inverse = InverseBidirectionalConverter.inverse(delegateMock);
        Assert.assertSame(target, inverse.restore(source));

        EasyMock.verify(delegateMock);
    }

    @Test
    public void testInvalidConstruction() {
        final BidirectionalConverter<String, Integer> invalidDelegate = null;

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                InverseBidirectionalConverter.inverse(invalidDelegate);
            }
        });
    }
}
