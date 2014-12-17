/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import com.hp.util.common.Converter;
import com.hp.util.common.ParameterizedFactory;
import com.hp.util.test.ThrowableTester;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class CollectionConverterTest {

    @Test
    public void testConvertCollection() {
        Set<Integer> source = new HashSet<Integer>();
        source.add(Integer.valueOf(1));
        source.add(Integer.valueOf(2));

        Converter<Integer, String> converter = new Converter<Integer, String>() {
            @Override
            public String convert(Integer s) {
                return String.valueOf(s.intValue());
            }
        };

        Set<String> target = CollectionConverter.convert(source, converter,
                CollectionConverter.<String> getHashSetFactory());
        Assert.assertNotNull(target);
        Assert.assertEquals(source.size(), target.size());
        for (Integer element : source) {
            Assert.assertTrue(target.contains(converter.convert(element)));
        }
    }

    @Test
    public void testConvertCollectionCustomFactory() {
        Set<Integer> source = new HashSet<Integer>();
        source.add(Integer.valueOf(1));
        source.add(Integer.valueOf(2));

        Converter<Integer, String> converter = new Converter<Integer, String>() {
            @Override
            public String convert(Integer s) {
                return String.valueOf(s.intValue());
            }
        };

        ParameterizedFactory<Set<String>, Integer> factory = new ParameterizedFactory<Set<String>, Integer>() {
            @Override
            public Set<String> create(Integer capacity) {
                return new HashSet<String>(capacity.intValue());
            }
        };

        Set<String> target = CollectionConverter.convert(source, converter, factory);
        Assert.assertNotNull(target);
        Assert.assertEquals(source.size(), target.size());
        for (Integer element : source) {
            Assert.assertTrue(target.contains(converter.convert(element)));
        }
    }

    @Test
    public void testConvertCollectionNull() {
        List<Integer> source = null;

        Converter<Integer, String> converter = new Converter<Integer, String>() {
            @Override
            public String convert(Integer s) {
                return String.valueOf(s.intValue());
            }
        };

        List<String> target = CollectionConverter.convert(source, converter,
                CollectionConverter.<String> getArrayListFactory());
        Assert.assertNotNull(target);
        Assert.assertTrue(target.isEmpty());
    }

    @Test
    public void testConvertCollectionInvalid() {
        final Set<Integer> validSource = null;

        @SuppressWarnings("unchecked")
        final Converter<Integer, String> validConverter = EasyMock.createMock(Converter.class);
        final Converter<Integer, String> invalidConverter = null;

        @SuppressWarnings("unchecked")
        final ParameterizedFactory<Set<String>, Integer> validFactory = EasyMock.createMock(ParameterizedFactory.class);
        final ParameterizedFactory<Set<String>, Integer> invalidFactory = null;

        ThrowableTester.testThrows(NullPointerException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                CollectionConverter.convert(validSource, invalidConverter, validFactory);
            }
        });

        ThrowableTester.testThrows(NullPointerException.class, new ThrowableTester.Instruction() {
            @Override
            public void execute() throws Throwable {
                CollectionConverter.convert(validSource, validConverter, invalidFactory);
            }
        });
    }

    @Test
    public void testGetArrayListFactory() {
        ParameterizedFactory<List<String>, Integer> factory = CollectionConverter.getArrayListFactory();
        Assert.assertNotNull(factory);
        Assert.assertTrue(Collections.emptyList().getClass().isInstance(factory.create(Integer.valueOf(0))));
        Assert.assertTrue(ArrayList.class.isInstance(factory.create(Integer.valueOf(1))));
    }

    @Test
    public void testGetLinkedListFactory() {
        ParameterizedFactory<List<String>, Integer> factory = CollectionConverter.getLinkedListFactory();
        Assert.assertNotNull(factory);
        Assert.assertTrue(Collections.emptyList().getClass().isInstance(factory.create(Integer.valueOf(0))));
        Assert.assertTrue(LinkedList.class.isInstance(factory.create(Integer.valueOf(1))));
    }

    @Test
    public void testGetHashSetFactory() {
        ParameterizedFactory<Set<String>, Integer> factory = CollectionConverter.getHashSetFactory();
        Assert.assertNotNull(factory);
        Assert.assertTrue(Collections.emptySet().getClass().isInstance(factory.create(Integer.valueOf(0))));
        Assert.assertTrue(HashSet.class.isInstance(factory.create(Integer.valueOf(1))));
    }

    @Test
    public void testGetTreeSetFactory() {
        ParameterizedFactory<Set<String>, Integer> factory = CollectionConverter.getTreeSetFactory();
        Assert.assertNotNull(factory);
        Assert.assertTrue(Collections.emptySet().getClass().isInstance(factory.create(Integer.valueOf(0))));
        Assert.assertTrue(TreeSet.class.isInstance(factory.create(Integer.valueOf(1))));
    }
}
