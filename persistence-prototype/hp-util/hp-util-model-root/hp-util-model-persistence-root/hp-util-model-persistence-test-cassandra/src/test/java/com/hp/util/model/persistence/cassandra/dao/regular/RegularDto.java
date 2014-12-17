/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.dao.regular;

import com.hp.util.common.model.AbstractIdentifiable;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.cassandra.mock.EnumMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class RegularDto extends AbstractIdentifiable<RegularDto, String> {
    private String attributeString;
    private boolean attributeBoolean;
    private Long attributeLong;
    private Date attributeDate;
    private EnumMock attributeEnum;

    public RegularDto(Id<RegularDto, String> id) {
        super(id);
    }

    public RegularDto(Id<RegularDto, String> id, String attributeString, boolean attributeBoolean, Long attributeLong,
        Date attributeDate, EnumMock attributeEnum) {
        super(id);
        this.attributeString = attributeString;
        this.attributeBoolean = attributeBoolean;
        this.attributeLong = attributeLong;
        this.attributeDate = attributeDate;
        this.attributeEnum = attributeEnum;
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
}
