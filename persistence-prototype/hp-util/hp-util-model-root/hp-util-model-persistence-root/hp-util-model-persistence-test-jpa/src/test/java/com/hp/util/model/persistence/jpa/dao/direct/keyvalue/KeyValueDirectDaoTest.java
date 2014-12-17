/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.direct.keyvalue;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import com.hp.util.common.type.Date;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.jpa.dao.JpaKeyValueDaoTest;
import com.hp.util.model.persistence.jpa.dao.direct.keyvalue.KeyValueDirectEntity.EmbeddableCustomValueType;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;
import com.hp.util.test.RandomDataGenerator;

/**
 * @author Fabiel Zuniga
 */
public class KeyValueDirectDaoTest extends
        JpaKeyValueDaoTest<Long, KeyValueDirectEntity, KeyValueDirectDao> {

    @Override
    protected KeyValueDirectDao createDaoInstance() {
        return new KeyValueDirectDao();
    }

    @Override
    protected boolean isPrimaryKeyIntegrityConstraintViolationSupported() {
        return false;
    }

    @Override
    protected boolean isVersioned() {
        return false;
    }

    @Override
    protected KeyValueDirectEntity createIdentifiable(Id<KeyValueDirectEntity, Long> id) {
        return null;
    }

    @Override
    protected List<KeyValueDirectEntity> createIdentifiables(int count) {
        List<KeyValueDirectEntity> identifiables = new ArrayList<KeyValueDirectEntity>(count);

        RandomDataGenerator dataGenerator = new RandomDataGenerator();
        for (int i = 0; i < count; i++) {

            KeyValueDirectEntity entity = new KeyValueDirectEntity("String attribute " + dataGenerator.getInt(),
                    dataGenerator.getBoolean(), Long.valueOf(dataGenerator.getLong()), Date.currentTime(),
                    dataGenerator.getEnum(EnumMock.class), new EmbeddableCustomValueType("Custom value type " + i,
                            Long.valueOf(i)));

            /*
            List<String> valueTypeCollection = new ArrayList<String>(2);
            valueTypeCollection.add("Value type " + i + " 1");
            valueTypeCollection.add("Value type " + i + " 2");
            entity.setValueTypeCollection(valueTypeCollection);

            List<EmbeddableCustomValueType> customValueTypeObjectCollection = new ArrayList<EmbeddableCustomValueType>();
            customValueTypeObjectCollection.add(new EmbeddableCustomValueType("Custom value type " + i + " 1", Long
                    .valueOf(1)));
            customValueTypeObjectCollection.add(new EmbeddableCustomValueType("Custom value type " + i + " 2", Long
                    .valueOf(2)));
            entity.setCustomValueTypeCollection(customValueTypeObjectCollection);

            Map<Integer, String> valueTypeMap = new HashMap<Integer, String>(2);
            valueTypeMap.put(Integer.valueOf(1), "Value 1");
            valueTypeMap.put(Integer.valueOf(2), "Value 2");
            entity.setValueTypeMap(valueTypeMap);

            Map<EmbeddableCustomValueType, EmbeddableCustomValueType> customValueTypeMap = new HashMap<EmbeddableCustomValueType, EmbeddableCustomValueType>(
                    2);
            customValueTypeMap.put(new EmbeddableCustomValueType("Custom value type key 1", Long.valueOf(1)),
                    new EmbeddableCustomValueType("Custom value type value 1", Long.valueOf(1)));
            customValueTypeMap.put(new EmbeddableCustomValueType("Custom value type key 2", Long.valueOf(2)),
                    new EmbeddableCustomValueType("Custom value type value 2", Long.valueOf(2)));
            entity.setCustomValueTypeMap(customValueTypeMap);
            */

            identifiables.add(entity);
        }

        return identifiables;
    }

    @Override
    protected void modify(KeyValueDirectEntity identifiable) {
        RandomDataGenerator dataGenerator = new RandomDataGenerator();

        identifiable.setAttributeString("String attribute " + dataGenerator.getInt());
        identifiable.setAttributeBoolean(!identifiable.getAttributeBoolean());
        identifiable.setAttributeLong(Long.valueOf(dataGenerator.getLong()));
        identifiable.setAttributeDate(Date.currentTime());
        identifiable.setAttributeEnum(dataGenerator.getEnum(EnumMock.class));
        identifiable.setAttributeCustomValueType(new EmbeddableCustomValueType("Custom value type", Long
                .valueOf(dataGenerator.getLong())));

        /*
        List<String> valueTypeCollection = new ArrayList<String>(identifiable.getValueTypeCollection());
        valueTypeCollection.add("New value type: " + dataGenerator.getInt());
        identifiable.setValueTypeCollection(valueTypeCollection);

        List<EmbeddableCustomValueType> customValueTypeCollection = new ArrayList<EmbeddableCustomValueType>(
                identifiable.getCustomValueTypeCollection());
        customValueTypeCollection.clear();
        customValueTypeCollection.add(new EmbeddableCustomValueType("Custom value type", Long.valueOf(dataGenerator
                .getLong())));
        identifiable.setCustomValueTypeCollection(customValueTypeCollection);

        Map<Integer, String> valueTypeMap = new HashMap<Integer, String>(identifiable.getValueTypeMap());
        valueTypeMap.put(Integer.valueOf(Integer.MAX_VALUE), "New value: " + dataGenerator.getInt());
        identifiable.setValueTypeMap(valueTypeMap);

        Map<EmbeddableCustomValueType, EmbeddableCustomValueType> customValueTypeMap = new HashMap<EmbeddableCustomValueType, EmbeddableCustomValueType>(
                identifiable.getCustomValueTypeMap());
        customValueTypeMap.put(new EmbeddableCustomValueType("New custom value type key", Long.valueOf(1)),
                new EmbeddableCustomValueType("New custom value type value", Long.valueOf(1)));
        identifiable.setCustomValueTypeMap(customValueTypeMap);
        */
    }

    @Override
    protected void assertEqualState(KeyValueDirectEntity expected, KeyValueDirectEntity actual) {
        Assert.assertEquals(expected.getAttributeString(), actual.getAttributeString());
        Assert.assertEquals(expected.getAttributeBoolean(), actual.getAttributeBoolean());
        Assert.assertEquals(expected.getAttributeLong(), actual.getAttributeLong());
        Assert.assertEquals(expected.getAttributeDate(), actual.getAttributeDate());
        Assert.assertEquals(expected.getAttributeEnum(), actual.getAttributeEnum());
        Assert.assertEquals(expected.getAttributeCustomValueType(), actual.getAttributeCustomValueType());
        /*
        Assert.assertTrue(expected.getValueTypeCollection().equals(actual.getValueTypeCollection()));
        Assert.assertTrue(expected.getCustomValueTypeCollection().equals(actual.getCustomValueTypeCollection()));
        Assert.assertTrue(expected.getValueTypeMap().equals(actual.getValueTypeMap()));
        Assert.assertTrue(expected.getCustomValueTypeMap().equals(actual.getCustomValueTypeMap()));
         */
    }
}
