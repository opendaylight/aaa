/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.regular;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hp.util.common.type.Date;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;
import com.hp.util.model.persistence.jpa.dao.regular.RegularDto.CustomValueType;
import com.hp.util.model.persistence.jpa.entity.BaseEntity;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
@Entity
@Table(name = "regular_entity")
public class RegularEntity extends BaseEntity {

    // NOTE: Using SQL reserved words in tables or column names causes the table not to be created
    // and an exception is thrown: "user lacks privilege or object not found".

    /*
     * An entity is not expected to be thread-safe
     */

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

    /*
     * NOTE: Create an Entity for objects which is natural to have an identity and for which a DAO
     * will be created to perform queries on their own. Otherwise a value type relation can be
     * created. It is possible to load an entity without loading the objects it related too. If an
     * object cannot exist without its container and it is not possible to load the objects without
     * loading its container, then it could be a value type object and not an entity.
     */

    @Embedded
    // Attributes must be overridden because RegularEntity contains long_attribute, that will cause
    // duplicated columns.
    @AttributeOverrides({
            @AttributeOverride(name = "attributeString", column = @Column(name = "custom_value_type_string_attribute")),
            @AttributeOverride(name = "attributeLong", column = @Column(name = "custom_value_type_long_attribute")) })
    private EmbeddableCustomValueType attributeCustomValueType;

    // With JPA it is a good practice to initialize collections when they are declared.

    // Collection of collections for embeddable types is not possible.
    /*
     * JPA 2 spec - JSR-317 2.6 Collections of Embeddable Classes and Basic Types: An embeddable
     * class (including an embeddable class within another embeddable class) that is contained
     * within an element collection must not contain an element collection, nor may it contain a
     * relationship to an entity other than a many-to-one or one-to-one relationship
     */

    @ElementCollection
    @CollectionTable(name = "entity_mock_value_type_collection", joinColumns = @JoinColumn(name = "entity_mock_id"))
    @Column(name = "value_type")
    private List<String> valueTypeCollection = new ArrayList<String>();

    @ElementCollection
    @CollectionTable(name = "entity_mock_custom_value_type_collection", joinColumns = @JoinColumn(name = "entity_mock_id"))
    // "custom_value_type" column won't be actually created in the database. A table called "entity_mock_custom_value_type_collection"
    // will be created with the column "entity_mock_id" pointing to this entity.
    // @Column(name = "custom_value_type")
    private List<EmbeddableCustomValueType> customValueTypeCollection = new ArrayList<EmbeddableCustomValueType>();

    @ElementCollection
    @CollectionTable(name = "entity_mock_value_type_map", joinColumns = @JoinColumn(name = "entity_mock_id"))
    @MapKeyColumn(name = "map_key")
    @Column(name = "map_value")
    private Map<Integer, String> valueTypeMap = new HashMap<Integer, String>();

    @ElementCollection
    // Attributes must be overridden because the key and value are of the same type and they are
    // store
    // into the same table: entity_mock_custom_value_type_map.
    @AttributeOverrides({
            @AttributeOverride(name = "key.attributeString", column = @Column(name = "key_string_attribute")),
            @AttributeOverride(name = "key.attributeLong", column = @Column(name = "key_long_attribute")),
            @AttributeOverride(name = "value.attributeString", column = @Column(name = "value_string_attribute")),
            @AttributeOverride(name = "value.attributeLong", column = @Column(name = "value_long_attribute")) })
    @CollectionTable(name = "entity_mock_custom_value_type_map", joinColumns = @JoinColumn(name = "entity_mock_id"))
    private Map<EmbeddableCustomValueType, EmbeddableCustomValueType> customValueTypeMap = new HashMap<EmbeddableCustomValueType, EmbeddableCustomValueType>();

    /**
     * This constructor is provided because of a restriction imposed by JPA and should not be used.
     */
    @Deprecated
    public RegularEntity() {

    }

    public RegularEntity(String attributeString, boolean attributeBoolean, Long attributeLong, Date attributeDate,
            EnumMock attributeEnum, CustomValueType attributeCustomValueType) {
        this.attributeString = attributeString;
        this.attributeBoolean = attributeBoolean;
        this.attributeLong = attributeLong;
        this.attributeEnum = attributeEnum;
        setAttributeDate(attributeDate);
        setAttributeCustomValueType(attributeCustomValueType);
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

    public CustomValueType getAttributeCustomValueType() {
        if (this.attributeCustomValueType != null) {
            return this.attributeCustomValueType.getCustomValueType();
        }
        return null;
    }

    public void setAttributeCustomValueType(CustomValueType attributeCustomValueType) {
        this.attributeCustomValueType = null;
        if (attributeCustomValueType != null) {
            this.attributeCustomValueType = new EmbeddableCustomValueType(attributeCustomValueType);
        }
    }

    public void setValueTypeCollection(List<String> valueTypeCollection) {
        this.valueTypeCollection.clear();
        this.valueTypeCollection.addAll(valueTypeCollection);
    }

    public List<String> getValueTypeCollection() {
        return Collections.unmodifiableList(this.valueTypeCollection);
    }

    public void setCustomValueTypeCollection(List<CustomValueType> customValueTypeCollection) {
        this.customValueTypeCollection.clear();

        for (CustomValueType customValueTypeObject : customValueTypeCollection) {
            this.customValueTypeCollection.add(new EmbeddableCustomValueType(customValueTypeObject));
        }
    }

    public List<CustomValueType> getCustomValueTypeCollection() {
        List<CustomValueType> result = new ArrayList<CustomValueType>(this.customValueTypeCollection.size());
        for (EmbeddableCustomValueType embeddedCustomValueType : this.customValueTypeCollection) {
            result.add(embeddedCustomValueType.getCustomValueType());
        }
        return Collections.unmodifiableList(result);
    }

    public void setValueTypeMap(Map<Integer, String> valueTypeMap) {
        this.valueTypeMap.clear();

        for (Entry<Integer, String> entry : valueTypeMap.entrySet()) {
            this.valueTypeMap.put(entry.getKey(), entry.getValue());
        }
    }

    public Map<Integer, String> getValueTypeMap() {
        return Collections.unmodifiableMap(this.valueTypeMap);
    }

    public void setCustomValueTypeMap(Map<CustomValueType, CustomValueType> customValueTypeMap) {
        this.customValueTypeMap.clear();

        for (Entry<CustomValueType, CustomValueType> entry : customValueTypeMap.entrySet()) {
            this.customValueTypeMap.put(new EmbeddableCustomValueType(entry.getKey()), new EmbeddableCustomValueType(
                    entry.getValue()));
        }
    }

    public Map<CustomValueType, CustomValueType> getCustomValueTypeMap() {
        Map<CustomValueType, CustomValueType> result = new HashMap<CustomValueType, CustomValueType>(
                this.customValueTypeMap.size());
        for (Entry<EmbeddableCustomValueType, EmbeddableCustomValueType> entry : this.customValueTypeMap.entrySet()) {
            result.put(entry.getKey().getCustomValueType(), entry.getValue().getCustomValueType());
        }
        return Collections.unmodifiableMap(result);
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

        public EmbeddableCustomValueType(CustomValueType customValueType) {
            this.attributeString = customValueType.getAttributeString();
            this.attributeLong = customValueType.getAttributeLong();
        }

        public CustomValueType getCustomValueType() {
            return new CustomValueType(this.attributeString, this.attributeLong);
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
