/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.dao.regular;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.filter.ComparabilityCondition;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.filter.IntervalCondition;
import com.hp.util.common.filter.SetCondition;
import com.hp.util.common.filter.StringCondition;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Property;
import com.hp.util.model.persistence.cassandra.mock.EnumMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public final class RegularDtoFilter {

    /*
     * The filter is the contract of supported queries (Supported conditions and their combination).
     * Visitor pattern is used to get the actual filter.
     */
    private Filter filter;

    private RegularDtoFilter() {
    }

    public static RegularDtoFilter filterAll() {
        RegularDtoFilter filter = new RegularDtoFilter();
        filter.filter = new All();
        return filter;
    }

    public static RegularDtoFilter filterByAttributeString(StringCondition condition) {
        RegularDtoFilter filter = new RegularDtoFilter();
        filter.filter = new ByAttributeString(condition);
        return filter;
    }

    public static RegularDtoFilter filterByAttributeBoolean(EqualityCondition<Boolean> condition) {
        RegularDtoFilter filter = new RegularDtoFilter();
        filter.filter = new ByAttributeBoolean(condition);
        return filter;
    }

    public static RegularDtoFilter filterByAttributeLong(ComparabilityCondition<Long> condition) {
        RegularDtoFilter filter = new RegularDtoFilter();
        filter.filter = new ByAttributeLong(condition);
        return filter;
    }

    public static RegularDtoFilter filterByAttributeDate(IntervalCondition<Date> condition) {
        RegularDtoFilter filter = new RegularDtoFilter();
        filter.filter = new ByAttributeDate(condition);
        return filter;
    }

    public static RegularDtoFilter filterByAttributeEnum(SetCondition<EnumMock> condition) {
        RegularDtoFilter filter = new RegularDtoFilter();
        filter.filter = new ByAttributeEnum(condition);
        return filter;
    }

    public static RegularDtoFilter filterByAttributeEnumAndAttributeDate(SetCondition<EnumMock> attributeEnumCondition,
            IntervalCondition<Date> attributeDateCondition) {
        RegularDtoFilter filter = new RegularDtoFilter();
        filter.filter = new ByAttributeEnumAndAttributeDate(attributeEnumCondition, attributeDateCondition);
        return filter;
    }

    /**
     * Accepts a visitor.
     * 
     * @param visitor visitor
     * @return the result of the visit
     */
    public <T> T accept(Visitor<T> visitor) {
        return this.filter.accept(visitor);
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("filter", this.filter)
        );
    }

    private static abstract class Filter {
        protected StringCondition attributeStringCondition;
        protected EqualityCondition<Boolean> attributeBooleanCondition;
        protected ComparabilityCondition<Long> attributeLongCondition;
        protected IntervalCondition<Date> attributeDateCondition;
        protected SetCondition<EnumMock> attributeEnumCondition;

        protected abstract <T> T accept(Visitor<T> visitor);

        @Override
        public String toString() {
            return ObjectToStringConverter.toString(
                    this,
                    Property.valueOf("attributeStringCondition", this.attributeStringCondition),
                    Property.valueOf("attributeBooleanCondition", this.attributeBooleanCondition),
                    Property.valueOf("attributeLongCondition", this.attributeLongCondition),
                    Property.valueOf("attributeDateCondition", this.attributeDateCondition),
                    Property.valueOf("attributeEnumCondition", this.attributeEnumCondition)
            );
        }
    }

    public static class All extends Filter {

        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ByAttributeString extends Filter {
        public ByAttributeString(StringCondition condition) {
            if (condition == null) {
                throw new NullPointerException("condition cannot be null");
            }
            this.attributeStringCondition = condition;
        }

        public StringCondition getAttributeStringCondition() {
            return this.attributeStringCondition;
        }

        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ByAttributeBoolean extends Filter {
        public ByAttributeBoolean(EqualityCondition<Boolean> condition) {
            if (condition == null) {
                throw new NullPointerException("condition cannot be null");
            }
            this.attributeBooleanCondition = condition;
        }

        public EqualityCondition<Boolean> getAttributeBooleanCondition() {
            return this.attributeBooleanCondition;
        }

        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ByAttributeLong extends Filter {
        public ByAttributeLong(ComparabilityCondition<Long> condition) {
            if (condition == null) {
                throw new NullPointerException("condition cannot be null");
            }
            this.attributeLongCondition = condition;
        }

        public ComparabilityCondition<Long> getAttributeLongCondition() {
            return this.attributeLongCondition;
        }

        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ByAttributeDate extends Filter {
        public ByAttributeDate(IntervalCondition<Date> condition) {
            if (condition == null) {
                throw new NullPointerException("condition cannot be null");
            }
            this.attributeDateCondition = condition;
        }

        public IntervalCondition<Date> getAttributeDateCondition() {
            return this.attributeDateCondition;
        }

        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ByAttributeEnum extends Filter {
        public ByAttributeEnum(SetCondition<EnumMock> condition) {
            if (condition == null) {
                throw new NullPointerException("condition cannot be null");
            }
            this.attributeEnumCondition = condition;
        }

        public SetCondition<EnumMock> getAttributeEnumCondition() {
            return this.attributeEnumCondition;
        }

        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class ByAttributeEnumAndAttributeDate extends Filter {
        public ByAttributeEnumAndAttributeDate(SetCondition<EnumMock> attributeEnumCondition,
                IntervalCondition<Date> attributeDateCondition) {
            if (attributeEnumCondition == null) {
                throw new NullPointerException("attributeEnumCondition cannot be null");
            }
            if (attributeDateCondition == null) {
                throw new NullPointerException("attributeDateCondition cannot be null");
            }
            this.attributeEnumCondition = attributeEnumCondition;
            this.attributeDateCondition = attributeDateCondition;
        }

        public SetCondition<EnumMock> getAttributeEnumCondition() {
            return this.attributeEnumCondition;
        }

        public IntervalCondition<Date> getAttributeDateCondition() {
            return this.attributeDateCondition;
        }

        @Override
        protected <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    /**
     * Filter visitor.
     * 
     * @param <T> type of the result of the visit
     */
    public static interface Visitor<T> {

        /**
         * Visits the filter.
         * 
         * @param filter filter to visit
         * @return the result of the visit
         */
        public T visit(All filter);

        /**
         * Visits the filter.
         * 
         * @param filter filter to visit
         * @return the result of the visit
         */
        public T visit(ByAttributeString filter);

        /**
         * Visits the filter.
         * 
         * @param filter filter to visit
         * @return the result of the visit
         */
        public T visit(ByAttributeBoolean filter);

        /**
         * Visits the filter.
         * 
         * @param filter filter to visit
         * @return the result of the visit
         */
        public T visit(ByAttributeLong filter);

        /**
         * Visits the filter.
         * 
         * @param filter filter to visit
         * @return the result of the visit
         */
        public T visit(ByAttributeDate filter);

        /**
         * Visits the filter.
         * 
         * @param filter filter to visit
         * @return the result of the visit
         */
        public T visit(ByAttributeEnum filter);

        /**
         * Visits the filter.
         * 
         * @param filter filter to visit
         * @return the result of the visit
         */
        public T visit(ByAttributeEnumAndAttributeDate filter);
    }
}
