/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common;

import org.junit.Assert;
import org.junit.Test;

import com.hp.util.common.Interfaces.Builder;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class InterfacesTest {

    @Test
    public void testAll() {
        Interfaces<InterfaceA> interfacesInterfaceA = Interfaces.all(InterfaceA.class);
        Assert.assertNotNull(interfacesInterfaceA);
        Assert.assertEquals(1, interfacesInterfaceA.get().size());
        Assert.assertTrue(interfacesInterfaceA.get().contains(InterfaceA.class));

        Interfaces<SubInterfaceA> interfacesSubInterfaceA = Interfaces.all(SubInterfaceA.class);
        Assert.assertNotNull(interfacesSubInterfaceA);
        Assert.assertEquals(2, interfacesSubInterfaceA.get().size());
        Assert.assertTrue(interfacesSubInterfaceA.get().contains(InterfaceA.class));
        Assert.assertTrue(interfacesSubInterfaceA.get().contains(SubInterfaceA.class));

        Interfaces<TypeA> interfacesTypeA = Interfaces.all(TypeA.class);
        Assert.assertNotNull(interfacesTypeA);
        Assert.assertEquals(1, interfacesTypeA.get().size());
        Assert.assertTrue(interfacesTypeA.get().contains(InterfaceA.class));

        Interfaces<TypeSubA> interfacesTypeSubA = Interfaces.all(TypeSubA.class);
        Assert.assertNotNull(interfacesTypeSubA);
        Assert.assertEquals(1, interfacesTypeSubA.get().size());
        Assert.assertTrue(interfacesTypeSubA.get().contains(SubInterfaceA.class));

        Interfaces<TypeAB> interfacesTypeAB = Interfaces.all(TypeAB.class);
        Assert.assertNotNull(interfacesTypeAB);
        Assert.assertEquals(2, interfacesTypeAB.get().size());
        Assert.assertTrue(interfacesTypeAB.get().contains(InterfaceA.class));
        Assert.assertTrue(interfacesTypeAB.get().contains(InterfaceB.class));

        Interfaces<TypeSubAB> interfacesTypeSubAB = Interfaces.all(TypeSubAB.class);
        Assert.assertNotNull(interfacesTypeSubAB);
        Assert.assertEquals(2, interfacesTypeSubAB.get().size());
        Assert.assertTrue(interfacesTypeSubAB.get().contains(SubInterfaceA.class));
        Assert.assertTrue(interfacesTypeSubAB.get().contains(InterfaceB.class));

        Interfaces<TypeC> interfacesTypeC = Interfaces.all(TypeC.class);
        Assert.assertNotNull(interfacesTypeC);
        Assert.assertTrue(interfacesTypeC.get().isEmpty());
    }

    @Test
    public void testSingle() {
        Interfaces<TypeAB> interfacesTypeAB = Interfaces.single(InterfaceB.class);
        Assert.assertNotNull(interfacesTypeAB);
        Assert.assertEquals(1, interfacesTypeAB.get().size());
        Assert.assertTrue(interfacesTypeAB.get().contains(InterfaceB.class));
    }

    @Test(expected = NullPointerException.class)
    public void testSingleInvalidSubjectNull() {
        Interfaces.single(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSingleInvalidSubjectNotInterface() {
        Interfaces.single(TypeAB.class);
    }

    @Test
    public void testBuilderAdd() {
        Builder<TypeAB> builder = Interfaces.createBuilder();

        builder.add(InterfaceA.class);
        Interfaces<TypeAB> interfaces = builder.build();
        Assert.assertEquals(1, interfaces.get().size());
        Assert.assertTrue(interfaces.get().contains(InterfaceA.class));

        builder.add(InterfaceB.class);
        interfaces = builder.build();
        Assert.assertEquals(2, interfaces.get().size());
        Assert.assertTrue(interfaces.get().contains(InterfaceA.class));
        Assert.assertTrue(interfaces.get().contains(InterfaceB.class));
    }

    @Test(expected = NullPointerException.class)
    public void testBuilderAddInvalidNull() {
        Builder<TypeAB> builder = Interfaces.createBuilder();
        builder.add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderAddInvalidNotInterface() {
        Builder<TypeAB> builder = Interfaces.createBuilder();
        builder.add(TypeAB.class);
    }

    @Test
    public void testAllWithGenerics() {
        @SuppressWarnings("rawtypes")
        Interfaces<GenericType> interfacesGenericType = Interfaces.all(GenericType.class);
        Assert.assertNotNull(interfacesGenericType);
        Assert.assertEquals(1, interfacesGenericType.get().size());
        Assert.assertTrue(interfacesGenericType.get().contains(GenericInterface.class));
    }

    @Test
    public void testSingleWithGenerics() {
        Interfaces<GenericType<String>> interfacesGenericType = Interfaces.single(GenericInterface.class);
        Assert.assertNotNull(interfacesGenericType);
        Assert.assertEquals(1, interfacesGenericType.get().size());
        Assert.assertTrue(interfacesGenericType.get().contains(GenericInterface.class));

        Interfaces<GenericType<String>> interfacesGenericTypeWildcard = Interfaces.single(GenericInterface.class);
        Assert.assertNotNull(interfacesGenericTypeWildcard);
        Assert.assertEquals(1, interfacesGenericTypeWildcard.get().size());
        Assert.assertTrue(interfacesGenericTypeWildcard.get().contains(GenericInterface.class));
    }

    @Test
    public void testBuilderAddWithGenerics() {
        Builder<GenericType<String>> builder = Interfaces.createBuilder();
        builder.add(GenericInterface.class);
        Interfaces<GenericType<String>> interfaces = builder.build();
        Assert.assertEquals(1, interfaces.get().size());
        Assert.assertTrue(interfaces.get().contains(GenericInterface.class));

        Builder<GenericType<?>> builderWildCard = Interfaces.createBuilder();
        builderWildCard.add(GenericInterface.class);
        Interfaces<GenericType<?>> interfacesWildCard = builderWildCard.build();
        Assert.assertEquals(1, interfacesWildCard.get().size());
        Assert.assertTrue(interfacesWildCard.get().contains(GenericInterface.class));
    }

    private static interface InterfaceA {

    }

    private static interface SubInterfaceA extends InterfaceA {

    }

    private static interface InterfaceB {

    }

    private static class TypeA implements InterfaceA {

    }

    private static class TypeSubA implements SubInterfaceA {

    }

    private static class TypeAB implements InterfaceA, InterfaceB {

    }

    private static class TypeSubAB implements SubInterfaceA, InterfaceB {

    }

    private static class TypeC {

    }

    private static interface GenericInterface<T> {

    }

    private static class GenericType<T> implements GenericInterface<T> {

    }
}
