/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hp.util.common.Converter;
import com.hp.util.common.converter.ObjectToStringConverter;

/**
 * Sort specification.
 * 
 * @param <T> type of the attribute to sort by
 * @author Fabiel Zuniga
 */
public class SortSpecification<T> {

    private List<SortComponent<T>> components;

    /**
     * Creates a sort specification.
     */
    public SortSpecification() {
        this.components = new ArrayList<SortSpecification.SortComponent<T>>();
    }

    /**
     * Appends a sort by component.
     *
     * @param attribute attribute to sort by
     * @param sortOrder sort order
     */
    public void addSortComponent(T attribute, SortOrder sortOrder) {
        this.components.add(new SortComponent<T>(attribute, sortOrder));
    }

    /**
     * Gets the specification's sort components.
     *
     * @return the specification's sort components
     */
    public List<SortComponent<T>> getSortComponents() {
        return Collections.unmodifiableList(this.components);
    }

    /**
     * Converts this sort specification to a different 'sort by' data type.
     * 
     * @param converter converter
     * @return a sort specification with the new data type
     */
    public <D> SortSpecification<D> convert(Converter<T, D> converter) {
        SortSpecification<D> converted = new SortSpecification<D>();
        for (SortComponent<T> component : this.components) {
            converted.addSortComponent(converter.convert(component.getSortBy()), component.getSortOrder());
        }
        return converted;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this, 
                Property.valueOf("components", this.components)
        );
    }

    /**
     * Sort component.
     *
     * @param <T> type of the attribute to sort by
     */
    public static final class SortComponent<T> {
        private T sortBy;
        private SortOrder sortOrder;

        private SortComponent(T sortBy, SortOrder sortOrder) {
            this.sortBy = sortBy;
            this.sortOrder = sortOrder;
        }

        /**
         * Gets the attribute to sort by.
         *
         * @return the attribute to sort by
         */
        public T getSortBy() {
            return this.sortBy;
        }

        /**
         * Gets the sort order.
         *
         * @return the sort order
         */
        public SortOrder getSortOrder() {
            return this.sortOrder;
        }

        @Override
        public String toString() {
            return ObjectToStringConverter.toString(
                    this,
                    Property.valueOf("sortBy", this.sortBy),
                    Property.valueOf("sortOrder", this.sortOrder)
            );
        }
    }
}
