/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.cql;

import com.hp.util.common.Converter;

/**
 * CQL converter.
 * 
 * @author Fabiel Zuniga
 */
public class CqlConverter {

    /**
     * CQL data converter. This converter uses {@link Object#toString()} to get the string
     * representation.
     * 
     * @param <D> type of the value to convert to a CLQ string value
     */
    public static final class CqlStringConverter<D> implements Converter<D, String> {
        @SuppressWarnings("rawtypes")
        private static CqlStringConverter instance = new CqlStringConverter();

        private CqlStringConverter() {

        }

        /**
         * Returns the single instance of this class.
         * 
         * @return the single instance of this class
         */
        @SuppressWarnings("unchecked")
        public static <D> Converter<D, String> getInstance() {
            return instance;
        }

        @Override
        public String convert(D source) {
            return '\'' + source.toString() + '\'';
        }
    }

    /**
     * CQL {@link Number} converter.
     * 
     * @param <D> type of the value to convert to a CLQ string value
     */
    public static final class CqlNumberConverter<D extends Number> implements Converter<D, String> {
        @SuppressWarnings("rawtypes")
        private static CqlNumberConverter instance = new CqlNumberConverter();

        private CqlNumberConverter() {

        }

        /**
         * Returns the single instance of this class.
         * 
         * @return the single instance of this class
         */
        @SuppressWarnings("unchecked")
        public static <D extends Number> Converter<D, String> getInstance() {
            return instance;
        }

        @Override
        public String convert(D source) {
            return source.toString();
        }
    }
}
