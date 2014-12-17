/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.direct.regular;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hp.util.common.Identifiable;
import com.hp.util.common.type.Date;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
@Entity
@Table(name = "regular_direct_entity")
public class RegularDirectEntity implements Identifiable<RegularDirectEntity, Long> {

    @Id
    @GeneratedValue
    @Column(name = "Id")
    private Long id;

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

    @AttributeOverrides({
            @AttributeOverride(name = "attributeString", column = @Column(name = "direct_base_entity_custom_value_type_string_attribute")),
            @AttributeOverride(name = "attributeLong", column = @Column(name = "direct_base_entity_custom_value_type_long_attribute")) })
    private EmbeddableCustomValueType attributeCustomValueType;

    /**
     * This constructor is provided because of a restriction imposed by JPA and should not be used.
     */
    @Deprecated
    public RegularDirectEntity() {

    }

    public RegularDirectEntity(String attributeString, boolean attributeBoolean, Long attributeLong, Date attributeDate,
            EnumMock attributeEnum, EmbeddableCustomValueType attributeCustomValueType) {
        this.attributeString = attributeString;
        this.attributeBoolean = attributeBoolean;
        this.attributeLong = attributeLong;
        this.attributeEnum = attributeEnum;
        setAttributeDate(attributeDate);
        this.attributeCustomValueType = attributeCustomValueType;
    }

    /**
     * Returns the entity's id.
     * 
     * @return the id
     */
    public Long getEntityId() {
        return this.id;
    }

    @Override
    public <E extends RegularDirectEntity> com.hp.util.common.type.Id<E, Long> getId() {
        return com.hp.util.common.type.Id.valueOf(this.id);
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

    public EmbeddableCustomValueType getAttributeCustomValueType() {
        return this.attributeCustomValueType;
    }

    public void setAttributeCustomValueType(EmbeddableCustomValueType attributeCustomValueType) {
        this.attributeCustomValueType = attributeCustomValueType;
    }

    /**
     * User defined value-typed element (It is not considered an entity because it has no identity).
     * <p>
     * As a value type instances of this class has a dependent lifecycle, don't need their own
     * identity, and don't have to support shared references.
     * <p>
     * It is a good practice to implement equals() and hasCode() in JPA-related embedded entities.
     */
    @Embeddable
    public static class EmbeddableCustomValueType {
        @Column(name = "string_attribute")
        private String attributeString;

        @Column(name = "long_attribute")
        private Long attributeLong;

        public EmbeddableCustomValueType() {

        }

        public EmbeddableCustomValueType(String attributeString, Long attributeLong) {
            this.attributeString = attributeString;
            this.attributeLong = attributeLong;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.attributeLong == null) ? 0 : this.attributeLong.hashCode());
            result = prime * result + ((this.attributeString == null) ? 0 : this.attributeString.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            EmbeddableCustomValueType other = (EmbeddableCustomValueType)obj;

            if (this.attributeLong == null) {
                if (other.attributeLong != null) {
                    return false;
                }
            }
            else if (!this.attributeLong.equals(other.attributeLong)) {
                return false;
            }

            if (this.attributeString == null) {
                if (other.attributeString != null) {
                    return false;
                }
            }
            else if (!this.attributeString.equals(other.attributeString)) {
                return false;
            }

            return true;
        }
    }
}
