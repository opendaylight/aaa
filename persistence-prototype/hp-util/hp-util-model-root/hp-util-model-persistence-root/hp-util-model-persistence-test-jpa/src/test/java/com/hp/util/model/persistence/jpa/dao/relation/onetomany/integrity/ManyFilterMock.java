/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.relation.onetomany.integrity;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.Property;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.relation.onetomany.nointegrity.OneDto;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class ManyFilterMock extends FilterMock {

    private EqualityCondition<Id<OneDto, Long>> relativeCondition;

    public EqualityCondition<Id<OneDto, Long>> getRelativeCondition() {
        return this.relativeCondition;
    }

    public void setRelativeCondition(EqualityCondition<Id<OneDto, Long>> relativeCondition) {
        this.relativeCondition = relativeCondition;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("attributeStringCondition", getAttributeStringCondition()),
                Property.valueOf("attributeBooleanCondition", getAttributeBooleanCondition()),
                Property.valueOf("attributeLongCondition", getAttributeLongCondition()),
                Property.valueOf("attributeDateCondition", getAttributeDateCondition()),
                Property.valueOf("attributeEnumCondition", getAttributeEnumCondition()),
                Property.valueOf("relativeCondition", this.relativeCondition)
        );
    }
}
