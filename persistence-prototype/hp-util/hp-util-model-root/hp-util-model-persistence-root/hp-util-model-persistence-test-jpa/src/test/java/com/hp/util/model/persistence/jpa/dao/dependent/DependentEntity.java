/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.dependent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hp.util.common.type.Date;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;
import com.hp.util.model.persistence.jpa.entity.BaseVersionedEntity;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
@Entity
@Table(name = "dependent_entity")
public class DependentEntity extends BaseVersionedEntity {

    // By definition natural keys are not auto-generated.
    @Column(name = "natural_key", unique = true)
    private Long naturalKey;

    @Column(name = "string_attribute")
    private String attributeString;

    @Column(name = "boolean_attribute")
    private boolean attributeBoolean;

    @Column(name = "long_attribute")
    private Long attributeLong;

    @Column(name = "date_attribute")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date attributeDate;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "severity")
    private EnumMock attributeEnum;

    @ManyToOne
    @JoinColumn(name = "owner_fk")
    private IndependentEntity owner;

    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * This constructor is provided because of a restriction imposed by JPA and should not be used.
     */
    @Deprecated
    public DependentEntity() {

    }

    public DependentEntity(IndependentEntity owner, Long naturalKey, String attributeString,
        boolean attributeBoolean, Long attributeLong, Date attributeDate, EnumMock attributeEnum) {
        this.owner = owner;
        this.ownerId = owner.getId();
        this.naturalKey = naturalKey;
        this.attributeString = attributeString;
        this.attributeBoolean = attributeBoolean;
        this.attributeLong = attributeLong;
        this.attributeEnum = attributeEnum;
        setAttributeDate(attributeDate);
    }

    public Long getNaturalKey() {
        return this.naturalKey;
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
        return Date.valueOf(this.attributeDate);
    }

    public void setAttributeDate(Date attributeDate) {
        this.attributeDate = null;
        if (attributeDate != null) {
            this.attributeDate = attributeDate.toDate();
        }
    }

    public EnumMock getAttributeEnum() {
        return this.attributeEnum;
    }

    public void setAttributeEnum(EnumMock attributeEnum) {
        this.attributeEnum = attributeEnum;
    }

    public IndependentEntity getIndependent() {
        return this.owner;
    }

    public Long getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(Long owner) {
        this.ownerId = owner;
    }
}
