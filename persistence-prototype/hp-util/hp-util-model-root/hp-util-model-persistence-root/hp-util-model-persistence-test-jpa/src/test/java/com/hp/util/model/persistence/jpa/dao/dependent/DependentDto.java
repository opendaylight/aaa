/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.dependent;

import com.hp.util.common.model.AbstractIdentifiable;
import com.hp.util.common.model.Dependent;
import com.hp.util.common.model.Versionable;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class DependentDto extends AbstractIdentifiable<DependentDto, Long> implements Versionable,
        Dependent<Id<IndependentDto, Long>> {

    private Long version;
    private Long naturalKey;
    private String attributeString;
    private boolean attributeBoolean;
    private Long attributeLong;
    private Date attributeDate;
    private EnumMock attributeEnum;
    private Id<IndependentDto, Long> owner;

    public DependentDto(Id<IndependentDto, Long> owner, Long naturalKey, String attributeString,
            boolean attributeBoolean, Long attributeLong, Date attributeDate, EnumMock attributeEnum) {
        this(null, owner, naturalKey, attributeString, attributeBoolean, attributeLong, attributeDate, attributeEnum);
    }

    public DependentDto(Id<DependentDto, Long> id, Id<IndependentDto, Long> owner, Long naturalKey,
            String attributeString, boolean attributeBoolean, Long attributeLong, Date attributeDate,
            EnumMock attributeEnum) {
        super(id);
        this.owner = owner;
        this.naturalKey = naturalKey;
        this.attributeString = attributeString;
        this.attributeBoolean = attributeBoolean;
        this.attributeLong = attributeLong;
        this.attributeDate = attributeDate;
        this.attributeEnum = attributeEnum;
    }

    public Long getNaturalKey() {
        return this.naturalKey;
    }

    @Override
    public Long getVersion() {
        return this.version;
    }

    /**
     * Sets the version.
     * 
     * @param version the version
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    public String getAttributeString() {
        return this.attributeString;
    }

    public void setAttributeString(String attributeString) {
        this.attributeString = attributeString;
    }

    public boolean getAttributeBoolean() {
        return this.attributeBoolean;
    }

    public void setAttributeBoolean(boolean attributeBoolean) {
        this.attributeBoolean = attributeBoolean;
    }

    public Long getAttributeLong() {
        return this.attributeLong;
    }

    public void setAttributeLong(Long attributeLong) {
        this.attributeLong = attributeLong;
    }

    public Date getAttributeDate() {
        return this.attributeDate;
    }

    public void setAttributeDate(Date attributeDate) {
        this.attributeDate = attributeDate;
    }

    public EnumMock getAttributeEnum() {
        return this.attributeEnum;
    }

    public void setAttributeEnum(EnumMock attributeEnum) {
        this.attributeEnum = attributeEnum;
    }

    @Override
    public Id<IndependentDto, Long> getIndependent() {
        return this.owner;
    }
}
