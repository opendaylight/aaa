/*
 * Copyright (c) 2011 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;

import org.junit.Assert;

/**
 * Tester to test objects which implement Serializable.
 * <p>
 * Text taken from Effective Java by Joshua Bloch
 * <p>
 * The object Serialization API provides a framework for encoding (Serializing) objects as byte
 * streams and reconstructing (Deserializing) objects from their byte-stream encodings. Once an
 * object has been serialized, its encoding can be transmitted from one running virtual machine to
 * another or stored on disk for later deserialization.
 * <ul>
 * <li>Implementing the Serializable interface is not a decision to be undertaken lightly. It offers
 * real benefits. It is essential if a class is to participate in a framework that relies on
 * serialization for object transmission or persistence.</li>
 * <li>A major cost of implementing Serializable is that it decreases the flexibility to change a
 * class's implementation once it has been released.</li>
 * <li>When a class implements Serializable, its byte-stream encoding becomes part of its exported
 * API.</li>
 * <li>If you don't make the effort to design a custom serialized form, but merely accept the
 * default, the serialized form will forever be tied to the class's original internal
 * representation. The class's private and package private instance fields become part of its
 * exported API, and the practice of minimizing access to fields loses its effectiveness as a tool
 * for information hiding.</li>
 * <li>Even a well-designed serialized form places constraints on the evolution of a class; an
 * ill-designed serialized form can be crippling.</li>
 * <li>Deserialization is a "hidden constructor" with all of the same issues as other constructors.
 * It must be ensured that all of the in variants established by the constructors are met.</li>
 * <li>When a serializable class is revised, it is important to check that it is possible to
 * serialize an instance of the new release and deserialize it in old releases, and vice versa. In
 * addition to binary compatibility, semantic compatibility must be tested. Ensure both that the
 * serialization-deserialization process succeeds and that it results in faithful replica of the
 * original object.</li>
 * <li>Classes designed for inheritance should rarely implement Serializable, and interfaces should
 * rarely extend it. If a class or interface exists primarily to participate in a framework that
 * requires all participants to implement Serializable, then it makes perfect sense for the class or
 * interface to implement or extend Serializable.</li>
 * <li>If the class has invariants that would be violated if its instance fields were initialized to
 * their default values consider adding the following method:
 * 
 * <pre>
 * private void readObjectNoData() throws InvalidObjectException {
 *     throw new InvalidObjectException(&quot;Stream data required&quot;);
 * }
 * </pre>
 * 
 * </li>
 * <li>However, if a class that is designed for inheritance is not Serializable, it may be
 * impossible to write a Serializable subclass. Specifically, it will be impossible if the
 * superclass does not provide an accessible parameterless constructor.</li>
 * <li><strong>Inner classes should not implement serializable unless they are static.</strong>
 * Non-static inner classes use compiled-generated synthetic fields to store references to enclosing
 * instances and to store values of local variables from enclosing scopes.</li>
 * <li><strong>Consider using a custom serialized form.</strong> Do not accept the default
 * serialized form without first considering whether it is appropriate. Generally speaking, the
 * default serialized form should be accepted only if it is largely identical to the encoding that
 * would be chosen for a custom serialization. <strong>The default serialized form is likely to be
 * appropriate if an object's physical representation is identical to its logical content.</strong></li>
 * <li>Every serializable class has a unique identification number associated with it. If this
 * number is not specified by declaring a static final long field named
 * <strong>serialVersionUID</strong>, the system automatically generates it at runtime by applying a
 * complex procedure to the class. The automatically generated value is affected by the class'name,
 * the names of the interfaces it implements, and all of its public and protected members. If any of
 * these things is changed any way, for example, by adding a trivial convenience method, the
 * automatically generates serial version UID changes. If a serial version is not declared,
 * compatibility will be broken, resulting in an {@link InvalidClassException} at runtime. This
 * number must be changed when backwards compatibility is broken.</li>
 * <li>Even if it is decided the default serialized form is a appropriate, it is recommended to to
 * provide a readObject method to ensure invariants and security.
 * 
 * <pre>
 * private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
 *     stream.defaultReadObject();
 *     // Ensure invariants and security
 *     // ...
 * }
 * 
 * </pre>
 * 
 * </li>
 * <li>If the default serialized form is used, transient fields will be initialized to the default
 * value when an instance is deserialized: {@code null} for reference fields, zero for numeric
 * primitive fields, and false for boolean fields. If these values are unaceptable for any transient
 * fields, provide a readObject method (like the one above) that restore transient fields to
 * acceptable values. Alternatively, these fields can be lazily initialized the first time they are
 * used.</li>
 * <li>Whether or not the default serialized form is used, <strong>synchronization must be imposed
 * on object serialization if it is imposed on any other method that reads the entire state of the
 * object.</strong>
 * 
 * <pre>
 * // synchronized method if other methods use synchronization to read the entire state of the object
 * private synchronized void writeObject(ObjectOutputStream stream) throws IOException {
 *     stream.defaultWriteObject();
 * }
 * 
 * </pre>
 * 
 * </li>
 * <li>Regardless of what serialized form is used, declare an explicit serial version UID in every
 * {@link Serializable} class. This eliminates the serial version UID as potential source if
 * incompatibility. There is also a small performance benefit: If no serial version UID is provided,
 * an expensive computation is required to generate one at runtime.</li>
 * <li>{@code readObject} method is effectively another public constructor, and it demands all of
 * the same care as any other constructor. Check its arguments for validity, make defensive copies,
 * etc. Loosely speaking, {@code readObject} is a constructor that takes a byte stream as its sole
 * parameter. Throw {@link InvalidObjectException} if an invariant is broken.</li>
 * <li>When an object is deserialized, it is critical to defensively copy any field containing an
 * object reference that a client must not possess. Don't use the {@code writeUnshared} and
 * {@code readUnshared} methods from {@link ObjectOutputStream} and {@link ObjectInputStream}; they
 * are typically faster that defensive copying, but they don't provide the necessary safety
 * guarantee.</li>
 * <li><strong>A {@code readObject} method must not invoke an overridable method, directly or
 * indirectly.</strong> Overriding methods will run before the subclass state is deserialized.</li>
 * <li>For instance control (Singletons), prefer enum types to {@code readResolve}. If you depend on
 * {@code readResolve} for instance control, all instance fields with object reference types must be
 * declared transient. Otherwise it is possible for a determined attacker to secure a reference to
 * the deserialized object before its {@code readResolve} method runs. <strong>The use of
 * {@code readResolve} is not obsolete</strong>. If you have to write a serializable
 * instance-controlled (singleton) class whose instances are not known at compile time, you will not
 * be able to represent the class as an enum type.</li>
 * <li><strong>The accessibility of {@code readResolve} is significant.</strong>. If it is placed in
 * a final class it should be private. For nonfinal classes, if it is private it will not apply to
 * any subclasses. If it is package-private, it will apply only to subclasses in the same package.
 * If it is protected or public, it will apply to all subclasses that do not override it. If
 * {@code readResolve} is protected or public and a subclass does not override it, deserializing a
 * serialized subclass instance will produce a superclass instance, which is likely to case
 * {@link ClassCastException}.</li>
 * <li>Consider serialization proxies instead of serialized instances. Serialization proxy pattern
 * reduces security problems and errors. See ProxiedSerializable under the test folder for an
 * example.</li>
 * </ul>
 * 
 * @author Fabiel Zuniga
 */
public final class SerializabilityTester {

    private SerializabilityTester() {

    }

    /**
     * Test serialization's binary and semantic compatibility.
     *
     * @param <T> type of the serializable entity
     * @param serializable serializable object
     * @param semanticCompatibilityVerifier semantic compatibility verifier to assert that
     *            {@code original} and its replica are semantically compatible. If {@code null} is
     *            provided just binary compatibility will be tested (The only errors that will be
     *            caught via this test is situations where an non-serializable object reference with
     *            a non-null value is used - {@link NotSerializableException}). Thus it is highly
     *            recommended to pass a non-null {@code semanticCompatibilityVerifier}.
     */
    public static <T extends Serializable> void testSerialization(T serializable,
            SemanticCompatibilityVerifier<T> semanticCompatibilityVerifier) {
        byte[] serialization = null;

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(serializable);
            serialization = byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            // e.printStackTrace();
            Assert.fail("Serialization failure: " + e.toString());
        }

        Object replicaObj = null;
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serialization);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            replicaObj = objectInputStream.readObject();
        }
        catch (Exception e) {
            // e.printStackTrace();
            Assert.fail("Deserialization failure: " + e.toString());
        }

        if (semanticCompatibilityVerifier != null) {
            @SuppressWarnings("unchecked")
            T replica = (T) replicaObj;
            semanticCompatibilityVerifier.assertSemanticCompatibility(serializable, replica);
        }
    }

    /**
     * Tests portable serialization between a different version of a class and the current version.
     * <p>
     * Both binary and semantic compatibility are tested.
     * <p>
     * The current version is part of the class path and thus loaded using the default class loader.
     * <p>
     * Process:
     *
     * <pre>
     * Let ClassA and ClassA' be two different versions of the same class
     * Let objA be an instance of ClassA
     * 1. Serialize objA into the stream of bytes bA</li>
     * 2. Deserialize bA using ClassA' producing instance objA'</li>
     * 3. Serialize objA' into the stream of bytes bA'</li>
     * 4. Deserialize bA' using ClassA producing instance objA_2</li>
     * 5. Assert that objA (original) and objA_2 (replica) are semantically compatible (Not necessarily equals) </li>
     * </pre>
     *
     * @param <T> type of the serializable entity
     * @param portableClass portable class
     * @param original current version instance of the portable class
     * @param differentVersionPath path of the jar file containing a different version of
     *            {@code portableClass}
     * @param semanticCompatibilityVerifier semantic compatibility verifier to assert that
     *            {@code original} and its replica reconstructed from a different class version are
     *            semantically compatible
     * @throws Exception if errors happen while testing portable serialization
     */
    public static <T extends Serializable> void testPortableSerialization(Class<T> portableClass, T original,
            Path differentVersionPath, SemanticCompatibilityVerifier<T> semanticCompatibilityVerifier) throws Exception {
        if (portableClass == null) {
            throw new NullPointerException("portableClass cannot be null");
        }

        if (original == null) {
            throw new NullPointerException("original cannot be null");
        }

        if (differentVersionPath == null) {
            throw new NullPointerException("differentVersionPath cannot be null");
        }

        ClassLoader differentVersionLoader = new ClassReloader(differentVersionPath, Thread.currentThread()
                .getContextClassLoader(), portableClass.getName());

        // The following test is already considered in ClassReloaderTest
        // Class<?> differentVersionClass = differentVersionLoader.loadClass(portableClass.getName());
        // Assert.assertFalse(portableClass.equals(differentVersionClass));

        Serializable differentVersionInstance = null;
        Serializable portableReplica = null;

        byte[] serialization = null;

        // Serializes original

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(original);
            serialization = byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            // e.printStackTrace();
            Assert.fail("Serialization of original failure: " + e.toString());
        }

        // Deserializes original using differentVersionLoader to produce a different version instance

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serialization);
                ObjectInputStream objectInputStream = new CustomClassLoaderObjectInputStream(byteArrayInputStream,
                        differentVersionLoader)) {
            differentVersionInstance = (Serializable) objectInputStream.readObject();
        }
        catch (Exception e) {
            // e.printStackTrace();
            Assert.fail("Deserialization of original in different version failure: " + e.toString());
        }

        // Serializes different version instance

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(differentVersionInstance);
            serialization = byteArrayOutputStream.toByteArray();
        }
        catch (Exception e) {
            // e.printStackTrace();
            Assert.fail("Serialization of different version failure: " + e.toString());
        }

        // Deserializes different version bytes using original's classloader to produce a compatible version replica

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serialization);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            portableReplica = (Serializable) objectInputStream.readObject();
        }
        catch (Exception e) {
            // e.printStackTrace();
            Assert.fail("Deserialization of different version in original (replica) failure: " + e.toString());
        }

        /*
        System.out.println("Original: " + original);
        System.out.println("Different Version Instance: " + differentVersionInstance);
        System.out.println("Portable Replica: " + portableReplica);
        */

        if (semanticCompatibilityVerifier != null) {
            @SuppressWarnings("unchecked")
            T replica = (T) portableReplica;
            semanticCompatibilityVerifier.assertSemanticCompatibility(original, replica);
        }
    }

    /**
     * Semantic compatibility verifier.
     *
     * @param <T> type of the serializable class
     */
    public static interface SemanticCompatibilityVerifier<T extends Serializable> {

        /**
         * Asserts semantic compatibility. Ensure both that the serialization-deserialization
         * process succeeds and that it results in faithful replica of the original object.
         * <p>
         * <strong>Important points to consider:</strong>
         * <ul>
         * <li>{@code original} and {@code replica} are not necessarily equal, specially of
         * SemanticCompatibilityVerifier is used in
         * {@link SerializabilityTester#testPortableSerialization(Class, Serializable, Path, SemanticCompatibilityVerifier)}
         * since different versions of the class will be used to serialize/deserialize.</li>
         * <li>If object references are used in {@code original}, the reference set in
         * {@code replica} won't be the same. Thus, unless the referenced object overrides
         * {@link #equals(Object)} and all the object's attributes are part of the
         * {@link #equals(Object)} method, the following instruction will be invalid:
         *
         * <pre>
         * Assert.assertEquals(original.getMyObject(), replica.getMyObject()); // Invalid
         * </pre>
         *
         * Compare the reference's attributes instead:
         *
         * <pre>
         * Assert.assertEquals(original.getMyObject().getPrimitiveAttribute_1(), replica.getMyObject().getPrimitiveAttribute_1()); // Valid
         * ...
         * Assert.assertEquals(original.getMyObject().getPrimitiveAttribute_n(), replica.getMyObject().getPrimitiveAttribute_n()); // Valid
         *
         * Assert.assertEquals(original.getMyObject().getObjectAttribute_1().getPrimitiveAttribute_1, replica.getMyObject().getObjectAttribute_1().getPrimitiveAttribute_1()); // Valid
         * ...
         * Assert.assertEquals(original.getMyObject().getObjectAttribute_1().getPrimitiveAttribute_n, replica.getMyObject().getObjectAttribute_1().getPrimitiveAttribute_n()); // Valid
         * </pre>
         *
         * Object value types usually override {@link #equals(Object)} involving all attributes:
         *
         * <pre>
         * Assert.assertEquals(original.getMyObjectValueType(), replica.getMyObjectValueType()); // Valid
         * </pre>
         *
         * </li>
         * </ul>
         *
         * @param original original object
         * @param replica original object's replica reconstructed using deserialization
         */
        public void assertSemanticCompatibility(T original, T replica);
    }
}
