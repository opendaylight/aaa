/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.compositeid.preferred;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hp.util.common.type.Date;
import com.hp.util.common.type.Uid;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
@Entity
@Table(name = "preferred_composite_id_entity")
public class CompositeIdPreferredEntity {

    /*
     * Note: JPA does not allow entities having the same single name.
     */

    @Id
    private EmbeddableCompositeId id;

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

    /**
     * This constructor is provided because of a restriction imposed by JPA and should not be used.
     */
    @Deprecated
    public CompositeIdPreferredEntity() {

    }

    public CompositeIdPreferredEntity(CompositeId id, String attributeString, boolean attributeBoolean, Long attributeLong,
        Date attributeDate, EnumMock attributeEnum) {
        this.id = new EmbeddableCompositeId(id);
        this.attributeString = attributeString;
        this.attributeBoolean = attributeBoolean;
        this.attributeLong = attributeLong;
        this.attributeEnum = attributeEnum;
        setAttributeDate(attributeDate);
    }

    public CompositeId getId() {
        return this.id.toCompositeId();
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

    @Embeddable
    public static class EmbeddableCompositeId implements Serializable {
        private static final long serialVersionUID = 1L;

        @Column(name = "uid_key")
        private String uidKey;

        @Column(name = "int_key")
        private int intKey;

        public EmbeddableCompositeId() {

        }

        public EmbeddableCompositeId(CompositeId compositeId) {
            this.uidKey = compositeId.getUidKey().getValue().toString();
            this.intKey = compositeId.getIntKey();
        }

        public CompositeId toCompositeId() {
            return new CompositeId(Uid.valueOf(UUID.fromString(this.uidKey)), this.intKey);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.intKey;
            result = prime * result + ((this.uidKey == null) ? 0 : this.uidKey.hashCode());
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

            if (!(obj instanceof EmbeddableCompositeId)) {
                return false;
            }

            EmbeddableCompositeId other = (EmbeddableCompositeId) obj;

            if (this.intKey != other.intKey) {
                return false;
            }

            if (this.uidKey == null) {
                if (other.uidKey != null) {
                    return false;
                }
            }
            else if (!this.uidKey.equals(other.uidKey)) {
                return false;
            }

            return true;
        }
    }
}
