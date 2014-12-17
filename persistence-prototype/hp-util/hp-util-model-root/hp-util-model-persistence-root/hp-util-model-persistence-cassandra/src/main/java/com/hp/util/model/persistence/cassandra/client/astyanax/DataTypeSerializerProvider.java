/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hp.util.common.type.Date;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeTypeSerializer.Component;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeTypeSerializer.ComponentCommandVisitor;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.DataTypeCommandVisitor;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;
import com.netflix.astyanax.Serializer;
import com.netflix.astyanax.serializers.AbstractSerializer;
import com.netflix.astyanax.serializers.AsciiSerializer;
import com.netflix.astyanax.serializers.BigDecimalSerializer;
import com.netflix.astyanax.serializers.BigIntegerSerializer;
import com.netflix.astyanax.serializers.BooleanSerializer;
import com.netflix.astyanax.serializers.ByteSerializer;
import com.netflix.astyanax.serializers.BytesArraySerializer;
import com.netflix.astyanax.serializers.CharSerializer;
import com.netflix.astyanax.serializers.ComparatorType;
import com.netflix.astyanax.serializers.DateSerializer;
import com.netflix.astyanax.serializers.DoubleSerializer;
import com.netflix.astyanax.serializers.FloatSerializer;
import com.netflix.astyanax.serializers.IntegerSerializer;
import com.netflix.astyanax.serializers.LongSerializer;
import com.netflix.astyanax.serializers.ShortSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.serializers.TimeUUIDSerializer;
import com.netflix.astyanax.serializers.UUIDSerializer;

/**
 * @author Fabiel Zuniga
 */
class DataTypeSerializerProvider {

    /*
     * Serializers are used to write keys, column names and custom column values. For basic type
     * column values column decoders are used to read and a visitor is used to write using Astyanax
     * mutators.
     */

    private Map<BasicType<?>, Serializer<?>> basicTypeSerializers;

    @SuppressWarnings("rawtypes")
    private final DataTypeVisitor dataTypeVisitor = new DataTypeVisitor();

    public DataTypeSerializerProvider() {
        this.basicTypeSerializers = new HashMap<BasicType<?>, Serializer<?>>();

        this.basicTypeSerializers.put(BasicType.VOID, ByteSerializer.get());
        this.basicTypeSerializers.put(BasicType.BYTE, ByteSerializer.get());
        this.basicTypeSerializers.put(BasicType.BYTE_ARRAY, BytesArraySerializer.get());
        this.basicTypeSerializers.put(BasicType.STRING_ASCII, AsciiSerializer.get());
        this.basicTypeSerializers.put(BasicType.STRING_UTF8, StringSerializer.get());
        this.basicTypeSerializers.put(BasicType.INTEGER, IntegerSerializer.get());
        this.basicTypeSerializers.put(BasicType.LONG, LongSerializer.get());
        this.basicTypeSerializers.put(BasicType.UUID, UUIDSerializer.get());
        this.basicTypeSerializers.put(BasicType.TIME_UUID, TimeUUIDSerializer.get());
        this.basicTypeSerializers.put(BasicType.DATE, new ImmutableDateSerializer());
        this.basicTypeSerializers.put(BasicType.BOOLEAN, BooleanSerializer.get());
        this.basicTypeSerializers.put(BasicType.FLOAT, FloatSerializer.get());
        this.basicTypeSerializers.put(BasicType.DOUBLE, DoubleSerializer.get());
        this.basicTypeSerializers.put(BasicType.DECIMAL, BigDecimalSerializer.get());
        this.basicTypeSerializers.put(BasicType.BIG_INTEGER, BigIntegerSerializer.get());
        this.basicTypeSerializers.put(BasicType.CHAR, CharSerializer.get());
        this.basicTypeSerializers.put(BasicType.SHORT, ShortSerializer.get());
        this.basicTypeSerializers.put(BasicType.COUNTER_COLUMN, LongSerializer.get());
    }

    /**
     * Gets a serializer.
     * 
     * @param type type to get the serializer for
     * @return the serializer
     */
    public <D> Serializer<D> getSerializer(DataType<D> type) {
        @SuppressWarnings("unchecked")
        DataTypeCommandVisitor<D, Serializer<D>, Void> commandVisitor = this.dataTypeVisitor;
        return type.accept(commandVisitor, null);
    }

    @SuppressWarnings("unchecked")
    private <D> Serializer<D> getBasicTypeSerializer(BasicType<D> type) {
        Serializer<?> serializer = this.basicTypeSerializers.get(type);
        if (serializer == null) {
            throw new RuntimeException("Unknown basic type " + type);
        }
        return (Serializer<D>) serializer;
    }

    private <D> Serializer<D> getCompositeTypeSerializer(CompositeType<D> type) {
        return new CompositeSerializer<D>(type, this);
    }
    
    private class DataTypeVisitor<D> implements DataTypeCommandVisitor<D, Serializer<D>, Void> {

        @Override
        public Serializer<D> visit(BasicType<D> dataType, Void input) {
            return getBasicTypeSerializer(dataType);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Serializer<D> visit(EnumType<D> dataType, Void input) {
            return new EnumSerializer(dataType.getEnumClass());
        }

        @Override
        public Serializer<D> visit(CompositeType<D> dataType, Void input) {
            return getCompositeTypeSerializer(dataType);
        }
    }

    static final class EnumSerializer<D extends Enum<D>> implements Serializer<D> {

        private static Serializer<String> delegate = StringSerializer.get();
        private final Class<D> enumClass;

        private EnumSerializer(Class<D> enumClass) {
            this.enumClass = enumClass;
        }

        public static String encode(Enum<?> value) {
            return value != null ? value.name() : null;
        }

        public static <D extends Enum<D>> D decode(String code, Class<D> enumClass) {
            D value = null;

            if (code != null) {
                for (D enumValue : enumClass.getEnumConstants()) {
                    if (enumValue.name().equals(code)) {
                        value = enumValue;
                        break;
                    }
                }
            }
            return value;
        }

        @Override
        public D fromByteBuffer(ByteBuffer arg0) {
            return decode(delegate.fromByteBuffer(arg0), this.enumClass);
        }

        @Override
        public D fromBytes(byte[] arg0) {
            return decode(delegate.fromBytes(arg0), this.enumClass);
        }

        @Override
        public List<D> fromBytesList(List<ByteBuffer> arg0) {
            List<D> result = new LinkedList<D>();
            for (String encoded : delegate.fromBytesList(arg0)) {
                result.add(decode(encoded, this.enumClass));
            }
            return result;
        }

        @Override
        public <V> Map<D, V> fromBytesMap(Map<ByteBuffer, V> arg0) {
            Map<D, V> result = new HashMap<D, V>();
            for (Entry<String, V> entry : delegate.fromBytesMap(arg0).entrySet()) {
                result.put(decode(entry.getKey(), this.enumClass), entry.getValue());
            }
            return result;
        }

        @Override
        public List<D> fromBytesSet(Set<ByteBuffer> arg0) {
            List<D> result = new LinkedList<D>();
            for (String date : delegate.fromBytesSet(arg0)) {
                result.add(decode(date, this.enumClass));
            }
            return result;
        }

        @Override
        public ByteBuffer fromString(String arg0) {
            return delegate.fromString(arg0);
        }

        @Override
        public ComparatorType getComparatorType() {
            return delegate.getComparatorType();
        }

        @Override
        public ByteBuffer getNext(ByteBuffer arg0) {
            return delegate.getNext(arg0);
        }

        @Override
        public String getString(ByteBuffer arg0) {
            return delegate.getString(arg0);
        }

        @Override
        public ByteBuffer toByteBuffer(D arg0) {
            String subject = null;
            if (arg0 != null) {
                subject = encode(arg0);
            }
            return delegate.toByteBuffer(subject);
        }

        @Override
        public byte[] toBytes(D arg0) {
            String subject = null;
            if (arg0 != null) {
                subject = encode(arg0);
            }
            return delegate.toBytes(subject);
        }

        @Override
        public List<ByteBuffer> toBytesList(List<D> arg0) {
            List<String> list = new LinkedList<String>();
            for (D d : arg0) {
                list.add(encode(d));
            }
            return delegate.toBytesList(list);
        }

        @Override
        public List<ByteBuffer> toBytesList(Collection<D> arg0) {
            Collection<String> collection = new LinkedList<String>();
            for (D d : arg0) {
                collection.add(encode(d));
            }
            return delegate.toBytesList(collection);
        }

        @Override
        public List<ByteBuffer> toBytesList(final Iterable<D> arg0) {
            Iterable<String> iterableAdapter = new Iterable<String>() {
                @Override
                public Iterator<String> iterator() {
                    return new IteratorAdapter(arg0.iterator());
                }
            };
            return delegate.toBytesList(iterableAdapter);
        }

        @Override
        public <V> Map<ByteBuffer, V> toBytesMap(Map<D, V> arg0) {
            Map<String, V> map = new HashMap<String, V>();
            for (Entry<D, V> entry : arg0.entrySet()) {
                map.put(encode(entry.getKey()), entry.getValue());
            }
            return delegate.toBytesMap(map);
        }

        @Override
        public Set<ByteBuffer> toBytesSet(List<D> arg0) {
            List<String> list = new LinkedList<String>();
            for (D d : arg0) {
                list.add(encode(d));
            }
            return delegate.toBytesSet(list);
        }

        private class IteratorAdapter implements Iterator<String> {
            private Iterator<D> delegateIterator;

            public IteratorAdapter(Iterator<D> delegate) {
                this.delegateIterator = delegate;
            }

            @Override
            public boolean hasNext() {
                return this.delegateIterator.hasNext();
            }

            @Override
            public String next() {
                return encode(this.delegateIterator.next());
            }

            @Override
            public void remove() {
                this.delegateIterator.remove();
            }
        }
    }

    private static final class ImmutableDateSerializer implements Serializer<Date> {

        private static Serializer<java.util.Date> delegate = DateSerializer.get();

        @Override
        public Date fromByteBuffer(ByteBuffer arg0) {
            return Date.valueOf(delegate.fromByteBuffer(arg0));
        }

        @Override
        public Date fromBytes(byte[] arg0) {
            return Date.valueOf(delegate.fromBytes(arg0));
        }

        @Override
        public List<Date> fromBytesList(List<ByteBuffer> arg0) {
            List<Date> result = new LinkedList<Date>();
            for (java.util.Date date : delegate.fromBytesList(arg0)) {
                result.add(Date.valueOf(date));
            }
            return result;
        }

        @Override
        public <V> Map<Date, V> fromBytesMap(Map<ByteBuffer, V> arg0) {
            Map<Date, V> result = new HashMap<Date, V>();
            for (Entry<java.util.Date, V> entry : delegate.fromBytesMap(arg0).entrySet()) {
                result.put(Date.valueOf(entry.getKey()), entry.getValue());
            }
            return result;
        }

        @Override
        public List<Date> fromBytesSet(Set<ByteBuffer> arg0) {
            List<Date> result = new LinkedList<Date>();
            for (java.util.Date date : delegate.fromBytesSet(arg0)) {
                result.add(Date.valueOf(date));
            }
            return result;
        }

        @Override
        public ByteBuffer fromString(String arg0) {
            return delegate.fromString(arg0);
        }

        @Override
        public ComparatorType getComparatorType() {
            return delegate.getComparatorType();
        }

        @Override
        public ByteBuffer getNext(ByteBuffer arg0) {
            return delegate.getNext(arg0);
        }

        @Override
        public String getString(ByteBuffer arg0) {
            return delegate.getString(arg0);
        }

        @Override
        public ByteBuffer toByteBuffer(Date arg0) {
            java.util.Date subject = null;
            if (arg0 != null) {
                subject = arg0.toDate();
            }
            return delegate.toByteBuffer(subject);
        }

        @Override
        public byte[] toBytes(Date arg0) {
            java.util.Date subject = null;
            if (arg0 != null) {
                subject = arg0.toDate();
            }
            return delegate.toBytes(subject);
        }

        @Override
        public List<ByteBuffer> toBytesList(List<Date> arg0) {
            List<java.util.Date> list = new LinkedList<java.util.Date>();
            for (Date date : arg0) {
                list.add(date.toDate());
            }
            return delegate.toBytesList(list);
        }

        @Override
        public List<ByteBuffer> toBytesList(Collection<Date> arg0) {
            Collection<java.util.Date> collection = new LinkedList<java.util.Date>();
            for (Date date : arg0) {
                collection.add(date.toDate());
            }
            return delegate.toBytesList(collection);
        }

        @Override
        public List<ByteBuffer> toBytesList(final Iterable<Date> arg0) {
            Iterable<java.util.Date> iterableAdapter = new Iterable<java.util.Date>() {
                @Override
                public Iterator<java.util.Date> iterator() {
                    return new IteratorAdapter(arg0.iterator());
                }
            };
            return delegate.toBytesList(iterableAdapter);
        }

        @Override
        public <V> Map<ByteBuffer, V> toBytesMap(Map<Date, V> arg0) {
            Map<java.util.Date, V> map = new HashMap<java.util.Date, V>();
            for (Entry<Date, V> entry : arg0.entrySet()) {
                map.put(entry.getKey().toDate(), entry.getValue());
            }
            return delegate.toBytesMap(map);
        }

        @Override
        public Set<ByteBuffer> toBytesSet(List<Date> arg0) {
            List<java.util.Date> list = new LinkedList<java.util.Date>();
            for (Date date : arg0) {
                list.add(date.toDate());
            }
            return delegate.toBytesSet(list);
        }

        private static class IteratorAdapter implements Iterator<java.util.Date> {
            private Iterator<Date> delegateIterator;

            public IteratorAdapter(Iterator<Date> delegate) {
                this.delegateIterator = delegate;
            }

            @Override
            public boolean hasNext() {
                return this.delegateIterator.hasNext();
            }

            @Override
            public java.util.Date next() {
                return this.delegateIterator.next().toDate();
            }

            @Override
            public void remove() {
                this.delegateIterator.remove();
            }
        }
    }

    private static class CompositeSerializer<D> extends AbstractSerializer<D> {

        /*
         * com.netflix.astyanax.serializers.AnnotatedCompositeSerializer was used as example for
         * this serializer.
         */

        private static final byte END_OF_COMPONENT = 0;
        // private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0);
        private static final int DEFAULT_BUFFER_SIZE = 128;
        private static final int COMPONENT_OVERHEAD = 3;

        private final CompositeType<D> compositeType;
        private final DataTypeSerializerProvider serializerProvider;
        private final int bufferSize = DEFAULT_BUFFER_SIZE;

        public CompositeSerializer(CompositeType<D> compositeType, DataTypeSerializerProvider serializerProvider) {
            this.compositeType = compositeType;
            this.serializerProvider = serializerProvider;
        }

        private static <D> void validateCompositeSerialization(List<Component<D, ?>> serialization,
                CompositeType<D> compositeType) {
            if (compositeType.getBasicTypes().size() != serialization.size()) {
                throw new RuntimeException("Invalid composite serializer for type " + compositeType
                        + ". Invalid number of serialization components. Expected: "
                        + compositeType.getBasicTypes().size() + ", Actual: " + serialization.size());
            }

            List<BasicType<?>> compositeTypeComponents = compositeType.getBasicTypes();
            for (int i = 0; i < compositeTypeComponents.size(); i++) {
                BasicType<?> compositeTypeComponent = compositeTypeComponents.get(i);
                Component<D, ?> serializationComponent = serialization.get(i);
                if (!compositeTypeComponent.equals(serializationComponent.getType())) {
                    throw new RuntimeException("Invalid composite serializer for type " + compositeType
                            + ". Invalid serialization component in position (" + i + "). Expected: "
                            + compositeTypeComponent + ", Actual: " + serializationComponent.getType());
                }
            }
        }

        @Override
        public ByteBuffer toByteBuffer(D obj) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(this.bufferSize);

            List<Component<D, ?>> serialization = this.compositeType.getCompositeTypeSerializer().serialize(obj);
            validateCompositeSerialization(serialization, this.compositeType);

            for (Component<D, ?> component : serialization) {
                try {
                    // First, serialize the ByteBuffer for this component
                    ComponentCommandVisitor<D, ByteBuffer> componentVisitor = new ComponentCommandVisitor<D, ByteBuffer>() {
                        @Override
                        public <E> ByteBuffer visit(Component<D, E> c) {
                            Serializer<E> serializer = CompositeSerializer.this.serializerProvider
                                    .getBasicTypeSerializer(c.getType());
                            return serializer.toByteBuffer(c.getValue());
                        }
                    };

                    ByteBuffer componentByteBuffer = component.accept(componentVisitor);

                    if (componentByteBuffer == null) {
                        componentByteBuffer = ByteBuffer.allocate(0);
                    }

                    if (componentByteBuffer.limit() + COMPONENT_OVERHEAD > byteBuffer.remaining()) {
                        int exponent = (int) Math.ceil(Math.log(componentByteBuffer.limit() + COMPONENT_OVERHEAD
                                + byteBuffer.limit())
                                / Math.log(2));
                        int newBufferSize = (int) Math.pow(2, exponent);
                        ByteBuffer temp = ByteBuffer.allocate(newBufferSize);
                        byteBuffer.flip();
                        temp.put(byteBuffer);
                        byteBuffer = temp;
                    }
                    // Write the data: <length><data><0>
                    byteBuffer.putShort((short) componentByteBuffer.remaining());
                    byteBuffer.put(componentByteBuffer.slice());
                    byteBuffer.put(END_OF_COMPONENT);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            byteBuffer.flip();
            return byteBuffer;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public D fromByteBuffer(ByteBuffer originalByteBuffer) {
            ByteBuffer byteBuffer = originalByteBuffer.duplicate();
            List<Component<D, ?>> serialization = new ArrayList<Component<D, ?>>();

            try {
                for (BasicType<?> basicType : this.compositeType.getBasicTypes()) {
                    ByteBuffer data = getWithShortLength(byteBuffer);
                    if (data != null) {
                        if (data.remaining() > 0) {
                            Serializer<?> serializer = CompositeSerializer.this.serializerProvider
                                    .getBasicTypeSerializer(basicType);
                            Object attributeValue = serializer.fromByteBuffer(data);
                            serialization.add(new Component(basicType, attributeValue));
                        }
                        else {
                            // Null value was serializer
                            serialization.add(new Component(basicType, null));
                        }
                        byte end_of_component = byteBuffer.get();
                        if (end_of_component != END_OF_COMPONENT) {
                            throw new RuntimeException("Invalid composite column.  Expected END_OF_COMPONENT.");
                        }
                    }
                    else {
                        throw new RuntimeException("Missing component data in composite type. Expected data for type "
                                + basicType);
                    }
                }

                return this.compositeType.getCompositeTypeSerializer().deserialize(serialization);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public ComparatorType getComparatorType() {
            return ComparatorType.COMPOSITETYPE;
        }

        private static int getShortLength(ByteBuffer bb) {
            int length = (bb.get() & 0xFF) << 8;
            return length | (bb.get() & 0xFF);
        }

        private static ByteBuffer getWithShortLength(ByteBuffer bb) {
            int length = getShortLength(bb);
            return getBytes(bb, length);
        }

        private static ByteBuffer getBytes(ByteBuffer bb, int length) {
            ByteBuffer copy = bb.duplicate();
            copy.limit(copy.position() + length);
            bb.position(bb.position() + length);
            return copy;
        }

        /*
        private static CompositeRangeBuilder buildRange() {
            return new CompositeRangeBuilder() {
                private int position = 0;

                public void nextComponent() {
                    position++;
                }

                @Override
                protected void append(ByteBufferOutputStream out, Object value, Equality equality) {
                    ComponentSerializer<?> serializer = components.get(position);
                    // First, serialize the ByteBuffer for this component
                    ByteBuffer cb;
                    try {
                        cb = serializer.serializeValue(value);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    if (cb == null) {
                        cb = EMPTY_BYTE_BUFFER;
                    }

                    // Write the data: <length><data><0>
                    out.writeShort((short) cb.remaining());
                    out.write(cb.slice());
                    out.write(equality.toByte());
                }
            };
        }

        public <T1> RangeEndpoint makeEndpoint(T1 value, Equality equality) {
            RangeEndpoint endpoint = new RangeEndpoint() {
                private ByteBuffer out = ByteBuffer.allocate(bufferSize);
                private int position = 0;
                private boolean done = false;

                @Override
                public RangeEndpoint append(Object value, Equality equality) {
                    ComponentSerializer<?> serializer = components.get(position);
                    position++;
                    // First, serialize the ByteBuffer for this component
                    ByteBuffer cb;
                    try {
                        cb = serializer.serializeValue(value);
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    if (cb == null) {
                        cb = EMPTY_BYTE_BUFFER;
                    }

                    if (cb.limit() + COMPONENT_OVERHEAD > out.remaining()) {
                        int exponent = (int) Math.ceil(Math.log((double) (cb.limit() + COMPONENT_OVERHEAD)
                                / (double) out.limit())
                                / Math.log(2));
                        int newBufferSize = out.limit() * (int) Math.pow(2, exponent);
                        ByteBuffer temp = ByteBuffer.allocate(newBufferSize);
                        out.flip();
                        temp.put(out);
                        out = temp;
                    }

                    // Write the data: <length><data><0>
                    out.putShort((short) cb.remaining());
                    out.put(cb.slice());
                    out.put(equality.toByte());
                    return this;
                }

                @Override
                public ByteBuffer toBytes() {
                    if (!done) {
                        out.flip();
                        done = true;
                    }
                    return out;
                }
            };
            endpoint.append(value, equality);
            return endpoint;
        }
        */
    }
}
