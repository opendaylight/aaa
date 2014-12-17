/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.keyspace;

import java.util.List;

/**
 * Composite type serializer.
 * 
 * @param <D> type of the composite type
 * @author Fabiel Zuniga
 */
public interface CompositeTypeSerializer<D> {

    /**
     * Serializes a composite type.
     * 
     * @param compositeValue composite type to serialize
     * @return a list of components. The returned list must correspond with the order of basicTypes
     *         used to create the related {@link CompositeType}.
     */
    public List<Component<D, ?>> serialize(D compositeValue);

    /**
     * Deserializes a composite type.
     * 
     * @param components components used to reconstruct the composite type
     * @return a reconstructed composite type
     */
    public D deserialize(List<Component<D, ?>> components);
    
    /**
     * Composite type component. A component represents an attribute of the composite type.
     * 
     * @param <D> type of the composite type
     * @param <E> type of the component value
     */
    public static class Component<D, E> {
        private BasicType<E> type;
        private E value;

        /**
         * Creates a component.
         * 
         * @param type attribute type
         * @param value attribute value
         */
        public Component(BasicType<E> type, E value) {
            if (type == null) {
                throw new NullPointerException("type canot be null");
            }
            this.type = type;
            this.value = value;
        }

        /**
         * Returns the component's type.
         * 
         * @return the component's type
         */
        public BasicType<E> getType() {
            return this.type;
        }

        /**
         * Returns the component's value.
         * 
         * @return the component's value
         */
        public E getValue() {
            return this.value;
        }

        /**
         * Accepts a visitor.
         * 
         * @param visitor visitor
         */
        public void accept(ComponentVisitor<D> visitor) {
            /*
             * NOTE: This visitor wouldn't be needed if the operation was part of this class. Assume
             * there is an operation that uses type and value and thus require them to be of the
             * same type.
             */ 
             /* 
             * void serialize(BasicType<E> type, E value);
             */
            /*
             * If such operation was added as part of the component then the operation would work
             * even with wildcard components:
             */
            /*
             * Component<D, ?> component = ...;
             * component.serialize();
             */
            /*
             * However, since this serializer is independent of implementations, it is not possible
             * to add methods that are particular to an implementation (Astyanax methods for
             * example).
             */
            /*
             * See com.hp.demo.pattern.visitor.wildcard example.
             */
            visitor.visit(this);
        }

        /**
         * Accepts a command visitor.
         * 
         * @param visitor visitor
         * @return the command result
         */
        public <T> T accept(ComponentCommandVisitor<D, T> visitor) {
            /*
             * This visitor wouldn't be needed either. See comments above.
             */
            return visitor.visit(this);
        }
    }

    /**
     * Component visitor.
     * <p>
     * This visitor is useful to match the component type and value (get rid of the wildcard ?).
     * 
     * @param <D> type of the composite type
     */
    public static interface ComponentVisitor<D> {

        /**
         * Visits a component.
         * 
         * @param component component
         */
        public <E> void visit(Component<D, E> component);
    }

    /**
     * Component command visitor.
     * <p>
     * This visitor is useful to match the component type and value (get rid of the wildcard ?).
     * 
     * @param <D> type of the composite type
     * @param <T> type of the command result
     */
    public static interface ComponentCommandVisitor<D, T> {

        /**
         * Visits a component.
         * 
         * @param component component
         * @return the command result
         */
        public <E> T visit(Component<D, E> component);
    }
}
