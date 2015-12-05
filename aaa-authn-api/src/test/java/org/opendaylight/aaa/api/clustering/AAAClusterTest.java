/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.clustering;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by saichler@gmail.com on 12/5/15.
 */
public class AAAClusterTest {

    @Test
    public void testWriteCode() throws IOException, InterruptedException {

        new MyTestObjectSerializer();

        Listener l = new Listener();
        AAAClusterNode node1 = new AAAClusterNode(l,32100,32101);
        AAAClusterNode node2 = new AAAClusterNode(l,32102,32103);
        //Give Thread 500ms to start
        Thread.sleep(500);
        node1.connectTo("127.0.0.1",32102);
        node2.connectTo("127.0.0.1",32100);
        //Give Thread 500ms to start
        Thread.sleep(500);

        MyTestObject obj = new MyTestObject();

        obj.name = "Test1";
        obj.age = 23;
        obj.is = true;

        synchronized (l) {
            node2.writeObject(obj);
            l.wait();
        }

        node1.shutdown();
        node2.shutdown();

        Assert.assertNotNull(l.o);
        Assert.assertEquals(AAAObjectEncoder.OPERATION_WRITE,l.op);
        Assert.assertEquals("Test1",l.o.name);
        Assert.assertEquals(Integer.valueOf(23),l.o.age);
        Assert.assertTrue(l.o.is);
    }

    @Test
    public void testNullAttributes() throws IOException, InterruptedException {

        new MyTestObjectSerializer();

        Listener l = new Listener();
        AAAClusterNode node1 = new AAAClusterNode(l,32100,32101);
        AAAClusterNode node2 = new AAAClusterNode(l,32102,32103);
        //Give Thread 500ms to start
        Thread.sleep(500);
        node1.connectTo("127.0.0.1",32102);
        //Give Thread 500ms to start
        Thread.sleep(500);

        MyTestObject obj = new MyTestObject();

        synchronized (l) {
            node2.updateObject(obj);
            l.wait();
        }

        node1.shutdown();
        node2.shutdown();

        Assert.assertEquals(AAAObjectEncoder.OPERATION_UPDATE,l.op);
        Assert.assertNotNull(l.o);
        Assert.assertNull(l.o.name);
        Assert.assertNull(l.o.age);
        Assert.assertNull(l.o.is);
    }


    private class Listener implements AAAClusterListener {

        private MyTestObject o = null;
        private int op = -1;

        @Override
        public void receivedObject(Object object, int operation) {
            this.o = (MyTestObject)object;
            this.op = operation;
            synchronized(this){
                this.notifyAll();
            }
        }
    }

    public static class MyTestObject {
        String name;
        Integer age;
        Boolean is;
    }

    public static class MyTestObjectSerializer implements AAAObjectSerializer<MyTestObject> {

        public MyTestObjectSerializer(){
            AAAObjectEncoder.addSerializer(MyTestObject.class,this);
        }

        @Override
        public void encode(MyTestObject object, AAAByteArrayWrapper wrapper) {
            AAAObjectEncoder.encodeString(object.name,wrapper);
            AAAObjectEncoder.encodeInt32(object.age,wrapper);
            AAAObjectEncoder.encodeBoolean(object.is,wrapper);
        }

        @Override
        public MyTestObject decode(AAAByteArrayWrapper byteWrapper) {
            MyTestObject o = new MyTestObject();
            o.name = AAAObjectEncoder.decodeString(byteWrapper);
            o.age = AAAObjectEncoder.decodeInt32(byteWrapper);
            o.is = AAAObjectEncoder.decodeBoolean(byteWrapper);
            return o;
        }
    }
}
