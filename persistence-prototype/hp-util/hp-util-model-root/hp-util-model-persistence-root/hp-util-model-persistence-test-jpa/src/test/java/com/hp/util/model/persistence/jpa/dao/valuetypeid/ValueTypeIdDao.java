/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.valuetypeid;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.converter.DateConverter;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.net.MacAddress;
import com.hp.util.model.persistence.jpa.dao.JpaMappedKeyDao;
import com.hp.util.model.persistence.jpa.dao.JpaQueryPredicateGenerator;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class ValueTypeIdDao extends
        JpaMappedKeyDao<MacAddress, ValueTypeIdDto, String, ValueTypeIdEntity, FilterMock, SortKeyMock> {

    public ValueTypeIdDao() {
        super(ValueTypeIdEntity.class);
    }

    @Override
    protected MacAddress getId(ValueTypeIdEntity entity) {
        return entity.getId();
    }

    @Override
    protected ValueTypeIdEntity create(ValueTypeIdDto identifiable) {
        return new ValueTypeIdEntity(identifiable.getId().getValue(), identifiable.getAttributeString(),
                identifiable.getAttributeBoolean(), identifiable.getAttributeLong(), identifiable.getAttributeDate(),
                identifiable.getAttributeEnum());
    }

    @Override
    protected ValueTypeIdDto doConvert(ValueTypeIdEntity source) {
        return new ValueTypeIdDto(Id.<ValueTypeIdDto, MacAddress> valueOf(source.getId()), source.getAttributeString(),
            source.getAttributeBoolean(), source.getAttributeLong(), source.getAttributeDate(),
            source.getAttributeEnum());
    }

    @Override
    protected void conform(ValueTypeIdEntity target, ValueTypeIdDto source) {
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
    }

    @Override
    protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder, Root<ValueTypeIdEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<ValueTypeIdEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                ValueTypeIdEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeBooleanCondition(), ValueTypeIdEntity_.attributeBoolean, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), ValueTypeIdEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        ValueTypeIdEntity_.attributeDate, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), ValueTypeIdEntity_.attributeEnum, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super ValueTypeIdEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return ValueTypeIdEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return ValueTypeIdEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return ValueTypeIdEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return ValueTypeIdEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return ValueTypeIdEntity_.attributeEnum;
        }
        return null;
    }

    @Override
    protected String mapKey(MacAddress key) {
        return key.getValue();
    }
}
