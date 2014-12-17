/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import java.util.HashMap;
import java.util.Map;

import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.DataTypeCommandVisitor;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;

/**
 * @author Fabiel Zuniga
 */
class DataTypeClassProvider {

    private Map<BasicType<?>, String> basicTypeClasses;

    @SuppressWarnings("rawtypes")
    private final DataTypeVisitor dataTypeVisitor = new DataTypeVisitor();

    public DataTypeClassProvider() {
        this.basicTypeClasses = new HashMap<BasicType<?>, String>();

        this.basicTypeClasses.put(BasicType.VOID, "ByteType");
        this.basicTypeClasses.put(BasicType.BYTE, "ByteType");
        this.basicTypeClasses.put(BasicType.BYTE_ARRAY, "BytesType");
        this.basicTypeClasses.put(BasicType.STRING_ASCII, "AsciiType");
        this.basicTypeClasses.put(BasicType.STRING_UTF8, "UTF8Type");
        this.basicTypeClasses.put(BasicType.INTEGER, "IntegerType");
        this.basicTypeClasses.put(BasicType.LONG, "LongType");
        this.basicTypeClasses.put(BasicType.UUID, "UUIDType");
        this.basicTypeClasses.put(BasicType.TIME_UUID, "TimeUUIDType");
        this.basicTypeClasses.put(BasicType.DATE, "DateType");
        this.basicTypeClasses.put(BasicType.BOOLEAN, "BooleanType");
        this.basicTypeClasses.put(BasicType.FLOAT, "FloatType");
        this.basicTypeClasses.put(BasicType.DOUBLE, "DoubleType");
        this.basicTypeClasses.put(BasicType.DECIMAL, "DecimalType");
        this.basicTypeClasses.put(BasicType.BIG_INTEGER, "BigIntegerType");
        this.basicTypeClasses.put(BasicType.CHAR, "CharType");
        this.basicTypeClasses.put(BasicType.SHORT, "ShortType");
        this.basicTypeClasses.put(BasicType.COUNTER_COLUMN, "CounterColumnType");
    }

    /**
     * Gets the data type class.
     * 
     * @param type data type to get the class for
     * @return the data type class
     */
    public <D> String getDataTypeClass(DataType<D> type) {
        @SuppressWarnings("unchecked")
        DataTypeCommandVisitor<D, String, Void> commandVisitor = this.dataTypeVisitor;
        return type.accept(commandVisitor, null);
    }

    private String getBasicTypeClass(BasicType<?> type) {
        String typeClass = this.basicTypeClasses.get(type);
        if (typeClass == null) {
            throw new RuntimeException("Unknown basic type " + type);
        }
        return typeClass;
    }

    private String getCompositeTypeClass(CompositeType<?> type) {
        StringBuilder str = new StringBuilder(32);
        str.append("CompositeType(");

        for (BasicType<?> basicType : type.getBasicTypes()) {
            if (basicType == null) {
                throw new NullPointerException("basicType cannot be null");
            }
            str.append(getBasicTypeClass(basicType));
            str.append(',');
        }

        str.delete(str.length() - 1, str.length());

        str.append(')');

        return str.toString();
    }

    private class DataTypeVisitor<D> implements DataTypeCommandVisitor<D, String, Void> {

        @Override
        public String visit(BasicType<D> dataType, Void input) {
            return getBasicTypeClass(dataType);
        }

        @Override
        public String visit(EnumType<D> dataType, Void input) {
            return getBasicTypeClass(BasicType.STRING_UTF8);
        }

        @Override
        public String visit(CompositeType<D> dataType, Void input) {
            return getCompositeTypeClass(dataType);
        }
    }
}
