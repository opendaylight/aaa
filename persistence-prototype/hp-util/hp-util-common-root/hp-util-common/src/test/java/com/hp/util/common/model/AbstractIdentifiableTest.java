/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.model;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.util.common.type.Id;
import com.hp.util.test.EqualityTester;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class AbstractIdentifiableTest {

    @Test
    public void testGetId() {
        ConcreteIdentifiable identifiable = new ConcreteIdentifiable();
        Assert.assertNull(identifiable.getId());

        Id<ConcreteIdentifiable, Long> id = Id.valueOf(Long.valueOf(1));
        identifiable = new ConcreteIdentifiable(id);
        Assert.assertEquals(id, identifiable.getId());
    }

    @Test
    public void testEqualsAndHashCode() {

        Id<ConcreteIdentifiable, Long> id1 = Id.valueOf(Long.valueOf(1));
        Id<ConcreteIdentifiable, Long> id2 = Id.valueOf(Long.valueOf(2));

        ConcreteIdentifiable obj = new ConcreteIdentifiable(id1);
        ConcreteIdentifiable equal1 = new ConcreteIdentifiable(id1);
        ConcreteIdentifiable equal2 = new ConcreteIdentifiable(id1);
        ConcreteIdentifiable unequal1 = new ConcreteIdentifiable(id2);
        ConcreteIdentifiable unequal2 = new ConcreteIdentifiable();

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1, unequal2);

        obj = new ConcreteIdentifiable();
        equal1 = new ConcreteIdentifiable();
        equal2 = new ConcreteIdentifiable();
        unequal1 = new ConcreteIdentifiable(id1);
        unequal2 = new ConcreteIdentifiable(id2);

        EqualityTester.testEqualsAndHashCode(obj, equal1, equal2, unequal1, unequal2);
    }

    @Test
    public void testToString() {
        Id<ConcreteIdentifiable, Long> id = Id.valueOf(Long.valueOf(1));
        ConcreteIdentifiable identifiable = new ConcreteIdentifiable(id);
        Assert.assertFalse(identifiable.toString().isEmpty());

        identifiable = new ConcreteIdentifiable();
        Assert.assertFalse(identifiable.toString().isEmpty());
    }

    private static class ConcreteIdentifiable extends AbstractIdentifiable<ConcreteIdentifiable, Long> {

        public ConcreteIdentifiable() {
            super();
        }

        public ConcreteIdentifiable(Id<? extends ConcreteIdentifiable, Long> id) {
            super(id);
        }
    }
}
