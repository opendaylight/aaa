/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.test;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Date;

/**
 * Serialization proxy pattern example:
 * <p>
 * The decision to implement Serializable increases the likelihood of bugs and security problems,
 * because it causes instances to be created using an extralinguistic mechanism in place of ordinary
 * constructors. There is, however, a technique that greatly reduces these risks. This technique is
 * known as the serialization proxy pattern.
 * <p>
 * The serialization proxy pattern is reasonably straightforward. First, design a private static
 * nested class of the serializable class that concisely represents the logical state of an instance
 * of the enclosing class. This nested class, known as the serialization proxy, should have a single
 * constructor, whose parameter type is the enclosing class. This constructor merely copies the data
 * from its argument: it need not do any consistency checking or defensive copying. By design, the
 * default serialized form of the serialization proxy is the perfect serialized form of the
 * enclosing class. Both the enclosing class and its serialization proxy must be declared to
 * implement Serializable.
 * <p>
 * Next, add the following {@code writeReplace} method to the enclosing class. This method can be
 * copied verbatim into any class with a serialization proxy:
 * 
 * <pre>
 * private Object writeReplace() {
 *     return new SerializationProxy(this);
 * }
 * </pre>
 * 
 * The presence of this method causes the serialization system to emit a SerializationProxy instance
 * instead of an instance of the enclosing class. In other words, the writeReplace method translates
 * an instance of the enclosing class to its serialization proxy prior to serialization.
 * <p>
 * With this writeReplace method in place, the serialization system will never generate a serialized
 * instance of the enclosing class, but an attacker might fabricate one in an attempt to violate the
 * class's invariants. To guarantee that such an attack would fail, merely add this readObject
 * method to the enclosing class:
 * 
 * <pre>
 * private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
 *     throw new InvalidObjectException(&quot;Proxy required&quot;);
 * }
 * </pre>
 * 
 * Finally, provide a readResolve method on the SerializationProxy class that returns a logically
 * equivalent instance of the enclosing class. The presence of this method causes the serialization
 * system to translate the serialization proxy back into an instance of the enclosing class upon
 * deserialization.
 * <p>
 * This readResolve method creates an instance of the enclosing class using only its public API, and
 * therein lies the beauty of the pattern. It largely eliminates the extralinguistic character of
 * serialization, because the deserialized instance is created using the same constructors, static
 * factories, and methods as any other instance. This frees you from having to separately ensure
 * that deserialized instances obey the class's invariants. If the class's static factories or
 * constructors establish these invariants, and its instance methods maintain them, you've ensured
 * that the invariants will be maintained by serialization as well.
 * <p>
 * 
 * <pre>
 * private Object readResolve() {
 *     return new ProxiedSerializable(this.attr1, this.attr2);
 * }
 * </pre>
 * 
 * Like the defensive copying approach, the serialization proxy approach stops the bogus byte-stream
 * attack and the internal field theft attack dead in their tracks. This approach allows the fields
 * of ProxiedSerializable to be final, which is required in order for the ProxiedSerializable class
 * to be truly immutable. <strong>Serialization brakes immutable (final) fields because they are
 * updated during deserialization. Using a Serialization Proxy immutable fields are really immutable
 * because the instance is created using a constructor or a factory method </strong>.
 * <p>
 * You don't have to figure out which fields might be compromised by devious serialization attacks,
 * nor do you have to explicitly perform validity checking as part of deserialization.
 * <p>
 * There is another way in which the serialization proxy pattern is more powerful than defensive
 * copying. The serialization proxy pattern allows the deserialized instance to have a different
 * class from the originally serialized instance. You might not think that this would be useful in
 * practice, but it is.
 * <p>
 * Consider the case of EnumSet. This class has no public constructors, only static factories. From
 * the client's perspective, they return EnumSet instances, but in fact, they return one of two
 * subclasses, depending on the size of the underlying enum type. If the underlying enum type has
 * sixty-four or fewer elements, the static factories return a RegularEnumSet; otherwise, they
 * return a JumboEnumSet. Now consider what happens if you serialize an enum set whose enum type has
 * sixty elements, then add five more elements to the enum type, and then deserialize the enum set.
 * It was a RegularEnumSet instance when it was serialized, but it had better be a JumboEnumSet
 * instance once it is deserialized. In fact that's exactly what happens, because EnumSet uses the
 * serialization proxy pattern.
 * <p>
 * <strong>The serialization proxy pattern has two limitations.</strong> It is not compatible with
 * classes that are extendable by their clients. Also, it is not compatible with some classes whose
 * object graphs contain circularities: if you attempt to invoke a method on an object from within
 * its serialization proxy's readResolve method, you'll get a ClassCastException, as you don't have
 * the object yet, only its serialization proxy.
 * <p>
 * Finally, the added power and safety of the serialization proxy pattern are not free. It is more
 * expensive to serialize and deserialize instances with serialization proxies than it is with
 * defensive copying.
 * <p>
 * In summary, consider the serialization proxy pattern whenever you find yourself having to write a
 * readObject or writeObject method on a class that is not extendable by its clients. This pattern
 * is perhaps the easiest way to robustly serialize objects with nontrivial invariants.
 * 
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class ProxiedSerializable implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Date attr1;
    private final Date attr2;

    public ProxiedSerializable(Date attr1, Date attr2) {
        // Constructor that helps keeping the class invariants
        if (attr1 == null) {
            throw new NullPointerException("attr1 cannot be null");
        }

        if (attr2 == null) {
            throw new NullPointerException("attr2 cannot be null");
        }

        if (attr1.equals(attr2)) {
            throw new IllegalArgumentException("attr1 cannot be equals to attr2");
        }

        this.attr1 = new Date(attr1.getTime());
        this.attr2 = new Date(attr2.getTime());
    }

    public Date getAttr1() {
        return new Date(this.attr1.getTime());
    }

    public Date getAttr2() {
        return new Date(this.attr2.getTime());
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    @SuppressWarnings("unused")
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Date attr1;
        private final Date attr2;

        SerializationProxy(ProxiedSerializable subject) {
            this.attr1 = subject.attr1;
            this.attr2 = subject.attr2;
        }

        private Object readResolve() {
            return new ProxiedSerializable(this.attr1, this.attr2);
        }
    }
}