/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cluster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by saichler@gmail.com on 12/4/15.
 */
public class AAAObjectEncoder {

    public static final int OPERATION_WRITE  = 1;
    public static final int OPERATION_UPDATE = 2;
    public static final int OPERATION_DELETE = 3;

    private static volatile Map<AAAMD5Identifier,Class<?>> idToClass = new ConcurrentHashMap<AAAMD5Identifier,Class<?>>();
    private static volatile Map<Class<?>,AAAObjectSerializer> classToSerializer = new ConcurrentHashMap<Class<?>,AAAObjectSerializer>();
    private static volatile Map<Class<?>,AAAMD5Identifier> classToId = new ConcurrentHashMap<Class<?>,AAAMD5Identifier>();

    public static final void addSerializer(final Class<?> objectClass,final AAAObjectSerializer serializer){
        final AAAMD5Identifier id = AAAMD5Identifier.createClassID(objectClass.getName());
        idToClass.put(id,objectClass);
        classToSerializer.put(objectClass,serializer);
        classToId.put(objectClass,id);
    }

    public static final byte[] encodeOperation(final int op,final Object object){
        AAAObjectSerializer serializer = classToSerializer.get(object.getClass());
        AAAMD5Identifier id = classToId.get(object.getClass());
        AAAByteArrayWrapper wrapper = new AAAByteArrayWrapper(1024);
        //encode the operation
        encodeInt32(op,wrapper);
        //encode the object type
        encodeInt64(id.getMd5Long1(),wrapper);
        encodeInt64(id.getMd5Long2(),wrapper);
        //encode the object
        serializer.encode(object,wrapper);
        return wrapper.getData();
    }

    public static final int getOperation(AAAByteArrayWrapper w){
        w.resetLocation();
        return decodeInt32(w);
    }

    public static final Object decodeObject(AAAByteArrayWrapper w){
        w.resetLocation();
        //Skip the operation
        w.advance(4);
        //Get the object code
        long md5Long1 = decodeInt64(w);
        long md5Long2 = decodeInt64(w);
        AAAMD5Identifier id = AAAMD5Identifier.createClassID(md5Long1,md5Long2);
        Class<?> objectClass = idToClass.get(id);
        AAAObjectSerializer serializer = classToSerializer.get(objectClass);
        return serializer.decode(w);
    }

    public static final void encodeInt32(int value, AAAByteArrayWrapper w) {
        w.adjustSize(4);
        w.getBytes()[w.getLocation()] = (byte) ((value >> 24) & 0xff);
        w.getBytes()[w.getLocation() + 1] = (byte) ((value >> 16) & 0xff);
        w.getBytes()[w.getLocation() + 2] = (byte) ((value >> 8) & 0xff);
        w.getBytes()[w.getLocation() + 3] = (byte) ((value >> 0) & 0xff);
        w.advance(4);
    }

    public static final int decodeInt32(AAAByteArrayWrapper w) {
        int value = (int) (0xff & w.getBytes()[w.getLocation()]) << 24
                  | (int) (0xff & w.getBytes()[w.getLocation() + 1]) << 16
                  | (int) (0xff & w.getBytes()[w.getLocation() + 2]) << 8
                  | (int) (0xff & w.getBytes()[w.getLocation() + 3]) << 0;
        w.advance(4);
        return value;
    }

    public static final void encodeInt64(long value,AAAByteArrayWrapper w) {
        w.adjustSize(8);
        w.getBytes()[w.getLocation()] = (byte) ((value >> 56) & 0xff);
        w.getBytes()[w.getLocation() + 1] = (byte) ((value >> 48) & 0xff);
        w.getBytes()[w.getLocation() + 2] = (byte) ((value >> 40) & 0xff);
        w.getBytes()[w.getLocation() + 3] = (byte) ((value >> 32) & 0xff);
        w.getBytes()[w.getLocation() + 4] = (byte) ((value >> 24) & 0xff);
        w.getBytes()[w.getLocation() + 5] = (byte) ((value >> 16) & 0xff);
        w.getBytes()[w.getLocation() + 6] = (byte) ((value >> 8) & 0xff);
        w.getBytes()[w.getLocation() + 7] = (byte) ((value >> 0) & 0xff);
        w.advance(8);
    }

    public static final long decodeInt64(final AAAByteArrayWrapper w) {
        long value = (long) (0xff & w.getBytes()[w.getLocation()]) << 56
                | (long) (0xff & w.getBytes()[w.getLocation() + 1]) << 48
                | (long) (0xff & w.getBytes()[w.getLocation() + 2]) << 40
                | (long) (0xff & w.getBytes()[w.getLocation() + 3]) << 32
                | (long) (0xff & w.getBytes()[w.getLocation() + 4]) << 24
                | (long) (0xff & w.getBytes()[w.getLocation() + 5]) << 16
                | (long) (0xff & w.getBytes()[w.getLocation() + 6]) << 8
                | (long) (0xff & w.getBytes()[w.getLocation() + 7]) << 0;
        w.advance(8);
        return value;
    }

    public static final void encodeString(final String value, final AAAByteArrayWrapper w) {
        byte bytes[] = value.getBytes();
        w.adjustSize(bytes.length+4);
        encodeInt32(bytes.length, w);
        System.arraycopy(bytes, 0, w.getBytes(), w.getLocation(), bytes.length);
        w.advance(bytes.length);
    }

    public static final String decodeString(final AAAByteArrayWrapper w) {
        int size = decodeInt32(w);
        byte arr[] = new byte[size];
        System.arraycopy(w.getBytes(),w.getLocation(),arr,0,arr.length);
        w.advance(arr.length);
        return new String(arr);
    }

    public static final void encodeBoolean(final boolean b,final AAAByteArrayWrapper w){
        w.adjustSize(1);
        if(b) {
            w.getBytes()[w.getLocation()] = 1;
        } else {
            w.getBytes()[w.getLocation()] = 0;
        }
        w.advance(1);
    }

    public static final boolean decodeBoolean(final AAAByteArrayWrapper w){
        if(w.getBytes()[w.getLocation()]==1){
            w.advance(1);
            return true;
        } else {
            w.advance(1);
            return false;
        }
    }
}
