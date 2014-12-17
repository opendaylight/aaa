/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.versioned;

import com.hp.util.common.model.AbstractIdentifiable;
import com.hp.util.common.model.Versionable;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;

/**
 * Transport object mock.
 * 
 * @author Fabiel Zuniga
 */
public class VersionedDto extends AbstractIdentifiable<VersionedDto, Long> implements Versionable {
    private Long version;
    private String attributeString;
    private boolean attributeBoolean;
    private Long attributeLong;
    private Date attributeDate;
    private EnumMock attributeEnum;

    /**
     * Creates a transport object.
     *
     * @param attributeString string type attribute.
     * @param attributeBoolean boolean type attribute.
     * @param attributeLong long type attribute.
     * @param attributeDate date type attribute.
     * @param attributeEnum enumeration type attribute.
     */
    public VersionedDto(String attributeString, boolean attributeBoolean, Long attributeLong, Date attributeDate,
        EnumMock attributeEnum) {
        this(null, attributeString, attributeBoolean, attributeLong, attributeDate, attributeEnum);
    }

    /**
     * Creates a transport object.
     *
     * @param id transfer object's id.
     * @param attributeString string type attribute.
     * @param attributeBoolean boolean type attribute.
     * @param attributeLong long type attribute.
     * @param attributeDate date type attribute.
     * @param attributeEnum enumeration type attribute.
     */
    public VersionedDto(Id<VersionedDto, Long> id, String attributeString, boolean attributeBoolean,
        Long attributeLong, Date attributeDate, EnumMock attributeEnum) {
        super(id);
        this.attributeString = attributeString;
        this.attributeBoolean = attributeBoolean;
        this.attributeLong = attributeLong;
        this.attributeDate = attributeDate;
        this.attributeEnum = attributeEnum;
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

    /**
     * Gets the string type attribute.
     *
     * @return the string type attribute.
     */
    public String getAttributeString() {
        return this.attributeString;
    }

    /**
     * Sets the string type attribute.
     *
     * @param attributeString the string type attribute.
     */
    public void setAttributeString(String attributeString) {
        this.attributeString = attributeString;
    }

    /**
     * Gets the boolean type attribute.
     *
     * @return the boolean type attribute.
     */
    public boolean getAttributeBoolean() {
        return this.attributeBoolean;
    }

    /**
     * Sets the boolean type attribute.
     *
     * @param attributeBoolean the boolean type attribute.
     */
    public void setAttributeBoolean(boolean attributeBoolean) {
        this.attributeBoolean = attributeBoolean;
    }

    /**
     * Gets the long type attribute.
     *
     * @return the long type attribute.
     */
    public Long getAttributeLong() {
        return this.attributeLong;
    }

    /**
     * Sets the long type attribute.
     *
     * @param attributeLong the long type attribute.
     */
    public void setAttributeLong(Long attributeLong) {
        this.attributeLong = attributeLong;
    }

    /**
     * Gets the date type attribute.
     *
     * @return the date type attribute.
     */
    public Date getAttributeDate() {
        return this.attributeDate;
    }

    /**
     * Sets the date type attribute.
     *
     * @param attributeDate the date type attribute.
     */
    public void setAttributeDate(Date attributeDate) {
        this.attributeDate = attributeDate;
    }

    /**
     * Gets the enumeration type type attribute.
     *
     * @return the enumeration type attribute.
     */
    public EnumMock getAttributeEnum() {
        return this.attributeEnum;
    }

    /**
     * Sets the enumeration type attribute.
     *
     * @param attributeEnum the enumeration type attribute.
     */
    public void setAttributeEnum(EnumMock attributeEnum) {
        this.attributeEnum = attributeEnum;
    }
}
