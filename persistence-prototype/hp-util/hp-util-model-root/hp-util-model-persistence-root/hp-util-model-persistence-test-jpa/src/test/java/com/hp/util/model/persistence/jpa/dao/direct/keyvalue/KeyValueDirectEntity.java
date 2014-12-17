/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.direct.keyvalue;

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
@Table(name = "key_value_direct_entity")
public class KeyValueDirectEntity implements Identifiable<KeyValueDirectEntity, Long> {

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

    /*
     * TODO: Hibernate doesn't allow fetching more than one bag because that would generate a
     * Cartesian product. Thus, in order to load the entire entity the fields need to be accessed
     * once the entity has been loaded. Using the data transfer pater this is done when the entity
     * is converted to the DTO. Thus, it is not possible to use this:
     * @ElementCollection(fetch = FetchType.EAGER) in more than one collection.
     */

    /*
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "key_value_direct_entity_value_type_collection", joinColumns = @JoinColumn(name = "entity_mock_id"))
    @Column(name = "value_type")
    private List<String> valueTypeCollection = new ArrayList<String>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "key_value_direct_entity_custom_value_type_collection", joinColumns = @JoinColumn(name = "entity_mock_id"))
    private List<EmbeddableCustomValueType> customValueTypeCollection = new ArrayList<EmbeddableCustomValueType>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "key_value_direct_entity_value_type_map", joinColumns = @JoinColumn(name = "entity_mock_id"))
    @MapKeyColumn(name = "map_key")
    @Column(name = "map_value")
    private Map<Integer, String> valueTypeMap = new HashMap<Integer, String>();

    @ElementCollection(fetch = FetchType.EAGER)
    @AttributeOverrides({
            @AttributeOverride(name = "key.attributeString", column = @Column(name = "key_string_attribute")),
            @AttributeOverride(name = "key.attributeLong", column = @Column(name = "key_long_attribute")),
            @AttributeOverride(name = "value.attributeString", column = @Column(name = "value_string_attribute")),
            @AttributeOverride(name = "value.attributeLong", column = @Column(name = "value_long_attribute")) })
    @CollectionTable(name = "key_value_direct_entity_custom_value_type_map", joinColumns = @JoinColumn(name = "entity_mock_id"))
    private Map<EmbeddableCustomValueType, EmbeddableCustomValueType> customValueTypeMap = new HashMap<EmbeddableCustomValueType, EmbeddableCustomValueType>();
    */

    /**
     * This constructor is provided because of a restriction imposed by JPA and should not be used.
     */
    @Deprecated
    public KeyValueDirectEntity() {

    }

    public KeyValueDirectEntity(String attributeString, boolean attributeBoolean, Long attributeLong, Date attributeDate,
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
    public <E extends KeyValueDirectEntity> com.hp.util.common.type.Id<E, Long> getId() {
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

    /*
    public void setValueTypeCollection(List<String> valueTypeCollection) {
        this.valueTypeCollection.clear();
        this.valueTypeCollection.addAll(valueTypeCollection);
    }

    public List<String> getValueTypeCollection() {
        return Collections.unmodifiableList(this.valueTypeCollection);
    }

    public void setCustomValueTypeCollection(List<EmbeddableCustomValueType> customValueTypeCollection) {
        this.customValueTypeCollection = new ArrayList<EmbeddableCustomValueType>(customValueTypeCollection);
    }

    public List<EmbeddableCustomValueType> getCustomValueTypeCollection() {
        return Collections.unmodifiableList(this.customValueTypeCollection);
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

    public void setCustomValueTypeMap(Map<EmbeddableCustomValueType, EmbeddableCustomValueType> customValueTypeMap) {
        this.customValueTypeMap = new HashMap<EmbeddableCustomValueType, EmbeddableCustomValueType>(customValueTypeMap);
    }

    public Map<EmbeddableCustomValueType, EmbeddableCustomValueType> getCustomValueTypeMap() {
        return Collections.unmodifiableMap(this.customValueTypeMap);
    }
    */

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
