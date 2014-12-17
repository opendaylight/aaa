/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.hp.util.common.Converter;
import com.hp.util.common.type.SortSpecification.SortComponent;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class SortSpecificationTest {

    @Test
    public void testSortComponents() {
        SortSpecification<Object> sortSpecification = new SortSpecification<Object>();

        List<SortComponent<Object>> sortComponents = sortSpecification.getSortComponents();

        Assert.assertNotNull(sortComponents);
        Assert.assertTrue(sortComponents.isEmpty());

        Object attribute1 = EasyMock.createMock(Object.class);
        Object attribute2 = EasyMock.createMock(Object.class);

        sortSpecification.addSortComponent(attribute1, SortOrder.ASCENDING);
        sortSpecification.addSortComponent(attribute2, SortOrder.DESCENDING);

        sortComponents = sortSpecification.getSortComponents();

        Assert.assertNotNull(sortComponents);
        Assert.assertEquals(2, sortComponents.size());

        SortComponent<Object> firstSortComponent = sortComponents.get(0);
        Assert.assertSame(attribute1, firstSortComponent.getSortBy());
        Assert.assertEquals(SortOrder.ASCENDING, firstSortComponent.getSortOrder());

        SortComponent<Object> secondSortComponent = sortComponents.get(1);
        Assert.assertSame(attribute2, secondSortComponent.getSortBy());
        Assert.assertEquals(SortOrder.DESCENDING, secondSortComponent.getSortOrder());
    }

    @Test
    public void testConvert() {
        SortSpecification<Integer> source = new SortSpecification<Integer>();
        source.addSortComponent(Integer.valueOf(1), SortOrder.ASCENDING);
        source.addSortComponent(Integer.valueOf(1), SortOrder.DESCENDING);

        Converter<Integer, String> converter = new Converter<Integer, String>() {
            @Override
            public String convert(Integer s) {
                return String.valueOf(s.intValue());
            }
        };

        SortSpecification<String> target = source.convert(converter);
        Assert.assertNotNull(target);

        List<SortComponent<Integer>> originalSortComponents = source.getSortComponents();
        List<SortComponent<String>> convertedSortComponents = target.getSortComponents();

        Assert.assertNotNull(convertedSortComponents);
        Assert.assertEquals(originalSortComponents.size(), convertedSortComponents.size());

        for (int i = 0; i < originalSortComponents.size(); i++) {
            SortComponent<Integer> originalSortComponent = originalSortComponents.get(i);
            SortComponent<String> convertedSortComponent = convertedSortComponents.get(i);

            Assert.assertEquals(converter.convert(originalSortComponent.getSortBy()),
                    convertedSortComponent.getSortBy());
            Assert.assertEquals(originalSortComponent.getSortOrder(), convertedSortComponent.getSortOrder());
        }
    }

    @Test
    public void testToString() {
        SortSpecification<Object> sortSpecification = new SortSpecification<Object>();
        sortSpecification.addSortComponent(new Object(), SortOrder.ASCENDING);
        sortSpecification.addSortComponent(new Object(), SortOrder.DESCENDING);
        Assert.assertFalse(sortSpecification.toString().isEmpty());

    }
}
