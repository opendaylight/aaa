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

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.model.AbstractIdentifiable;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.Property;
import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class RegularDto extends AbstractIdentifiable<RegularDto, Long> {

    private String attributeString;
    private boolean attributeBoolean;
    private Long attributeLong;
    private Date attributeDate;
    private EnumMock attributeEnum;
    private CustomValueType attributeCustomValueType;
    private List<String> valueTypeCollection;
    private List<CustomValueType> customValueTypeCollection;
    private Map<Integer, String> valueTypeMap;
    private Map<CustomValueType, CustomValueType> customValueTypeMap;

    public RegularDto(String attributeString, boolean attributeBoolean, Long attributeLong, Date attributeDate,
            EnumMock attributeEnum, CustomValueType attributeCustomValueType) {
        this(null, attributeString, attributeBoolean, attributeLong, attributeDate, attributeEnum,
                attributeCustomValueType);
    }

    public RegularDto(Id<RegularDto, Long> id, String attributeString, boolean attributeBoolean, Long attributeLong,
            Date attributeDate, EnumMock attributeEnum, CustomValueType attributeCustomValueType) {
        super(id);
        this.attributeString = attributeString;
        this.attributeBoolean = attributeBoolean;
        this.attributeLong = attributeLong;
        this.attributeDate = attributeDate;
        this.attributeEnum = attributeEnum;
        this.attributeCustomValueType = attributeCustomValueType;
        this.valueTypeCollection = new ArrayList<String>();
        this.customValueTypeCollection = new ArrayList<CustomValueType>();
        this.valueTypeMap = new HashMap<Integer, String>();
        this.customValueTypeMap = new HashMap<CustomValueType, CustomValueType>();
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

    public CustomValueType getAttributeCustomValueType() {
        return this.attributeCustomValueType;
    }

    public void setAttributeCustomValueType(CustomValueType attributeCustomValueType) {
        this.attributeCustomValueType = attributeCustomValueType;
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
        this.customValueTypeCollection.addAll(customValueTypeCollection);
    }

    public List<CustomValueType> getCustomValueTypeCollection() {
        return Collections.unmodifiableList(this.customValueTypeCollection);
    }

    public void setValueTypeMap(Map<Integer, String> mapType) {
        this.valueTypeCollection.clear();

        for (Entry<Integer, String> entry : mapType.entrySet()) {
            this.valueTypeMap.put(entry.getKey(), entry.getValue());
        }
    }

    public Map<Integer, String> getValueTypeMap() {
        return Collections.unmodifiableMap(this.valueTypeMap);
    }

    public void setCustomValueTypeMap(Map<CustomValueType, CustomValueType> customValueTypeMap) {
        this.customValueTypeMap.clear();
        this.customValueTypeMap.putAll(customValueTypeMap);
    }

    public Map<CustomValueType, CustomValueType> getCustomValueTypeMap() {
        return Collections.unmodifiableMap(this.customValueTypeMap);
    }

    /**
     * User defined value-typed element.
     * <p>
     * As a value type instances of this class has a dependent lifecycle, don't need their own
     * identity, and don't have to support shared references.
     * <p>
     * It is a good practice to implement equals() and hasCode() in JPA-related embedded objects.
     */
    public static class CustomValueType {
        private String attributeString;
        private Long attributeLong;

        public CustomValueType(String attributeString, Long attributeLong) {
            this.attributeString = attributeString;
            this.attributeLong = attributeLong;
        }

        public String getAttributeString() {
            return this.attributeString;
        }

        public void setAttributeString(String attributeString) {
            this.attributeString = attributeString;
        }

        public Long getAttributeLong() {
            return this.attributeLong;
        }

        public void setAttributeLong(Long attributeLong) {
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

            CustomValueType other = (CustomValueType)obj;

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

        @Override
        public String toString() {
            return ObjectToStringConverter.toString(
                    this,
                    Property.valueOf("attributeString", this.attributeString),
                    Property.valueOf("attributeLong", this.attributeLong)
            );
        }
    }
}
