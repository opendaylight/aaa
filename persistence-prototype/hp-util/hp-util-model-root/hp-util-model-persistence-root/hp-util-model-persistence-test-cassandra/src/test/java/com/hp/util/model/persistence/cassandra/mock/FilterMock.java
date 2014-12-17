/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.mock;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.filter.ComparabilityCondition;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.filter.IntervalCondition;
import com.hp.util.common.filter.SetCondition;
import com.hp.util.common.filter.StringCondition;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Property;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class FilterMock {

    private StringCondition attributeStringCondition;
    private EqualityCondition<Boolean> attributeBooleanCondition;
    private ComparabilityCondition<Long> attributeLongCondition;
    private IntervalCondition<Date> attributeDateCondition;
    private SetCondition<EnumMock> attributeEnumCondition;

    public StringCondition getAttributeStringCondition() {
        return this.attributeStringCondition;
    }

    public void setAttributeStringCondition(StringCondition attributeStringCondition) {
        this.attributeStringCondition = attributeStringCondition;
    }

    public EqualityCondition<Boolean> getAttributeBooleanCondition() {
        return this.attributeBooleanCondition;
    }

    public void setAttributeBooleanCondition(EqualityCondition<Boolean> attributeBooleanCondition) {
        this.attributeBooleanCondition = attributeBooleanCondition;
    }

    public ComparabilityCondition<Long> getAttributeLongCondition() {
        return this.attributeLongCondition;
    }

    public void setAttributeLongCondition(ComparabilityCondition<Long> attributeLongCondition) {
        this.attributeLongCondition = attributeLongCondition;
    }

    public IntervalCondition<Date> getAttributeDateCondition() {
        return this.attributeDateCondition;
    }

    public void setAttributeDateCondition(IntervalCondition<Date> attributeDateCondition) {
        this.attributeDateCondition = attributeDateCondition;
    }

    public SetCondition<EnumMock> getAttributeEnumCondition() {
        return this.attributeEnumCondition;
    }

    public void setAttributeEnumCondition(SetCondition<EnumMock> attributeEnumCondition) {
        this.attributeEnumCondition = attributeEnumCondition;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("attributeStringCondition", getAttributeStringCondition()),
                Property.valueOf("attributeBooleanCondition", getAttributeBooleanCondition()),
                Property.valueOf("attributeLongCondition", getAttributeLongCondition()),
                Property.valueOf("attributeDateCondition", getAttributeDateCondition()),
                Property.valueOf("attributeEnumCondition", getAttributeEnumCondition())
        );
    }
}
