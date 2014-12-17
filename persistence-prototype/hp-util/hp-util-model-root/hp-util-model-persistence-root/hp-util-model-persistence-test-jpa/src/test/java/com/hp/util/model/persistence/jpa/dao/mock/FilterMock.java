/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.mock;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.filter.ComparabilityCondition;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.filter.IntervalCondition;
import com.hp.util.common.filter.SetCondition;
import com.hp.util.common.filter.StringCondition;
import com.hp.util.common.filter.TimePeriodCondition;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Id;
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

    /*
     * TODO: This was added to allow testing JpaQueryPredicateGenerator
     */
    private TimePeriodCondition attributeDateConditionAsTimePeriod;

    /*
     * TODO: This was added to allow testing JpaQueryPredicateGenerator
     */
    private EqualityCondition<Id<Object, Long>> attributeLongConditionAsId;

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

    public TimePeriodCondition getAttributeDateConditionAsTimePeriod() {
        return this.attributeDateConditionAsTimePeriod;
    }

    public void setAttributeDateConditionAsTimePeriod(TimePeriodCondition attributeDateCondition) {
        this.attributeDateConditionAsTimePeriod = attributeDateCondition;
    }

    public EqualityCondition<Id<Object, Long>> getAttributeLongConditionAsId() {
        return this.attributeLongConditionAsId;
    }

    public void setAttributeLongConditionAsId(EqualityCondition<Id<Object, Long>> attributeLongCondition) {
        this.attributeLongConditionAsId = attributeLongCondition;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("attributeStringCondition", this.attributeStringCondition),
                Property.valueOf("attributeBooleanCondition", this.attributeBooleanCondition),
                Property.valueOf("attributeLongCondition", this.attributeLongCondition),
                Property.valueOf("attributeDateCondition", this.attributeDateCondition),
                Property.valueOf("attributeEnumCondition", this.attributeEnumCondition),
                Property.valueOf("attributeDateConditionAsTimePeriod", this.attributeDateConditionAsTimePeriod),
                Property.valueOf("attributeLongConditionAsId", this.attributeLongConditionAsId)
        );
    }
}
