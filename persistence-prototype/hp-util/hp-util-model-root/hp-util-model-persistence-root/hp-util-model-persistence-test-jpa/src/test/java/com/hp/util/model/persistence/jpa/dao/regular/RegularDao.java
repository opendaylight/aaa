/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.regular;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.converter.DateConverter;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.jpa.dao.JpaOffsetPageDao;
import com.hp.util.model.persistence.jpa.dao.JpaQueryPredicateGenerator;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class RegularDao extends JpaOffsetPageDao<Long, RegularDto, RegularEntity, FilterMock, SortKeyMock> {

    public RegularDao() {
        super(RegularEntity.class);
    }

    @Override
    protected Long getId(RegularEntity entity) {
        return entity.getId();
    }

    @Override
    protected RegularEntity create(RegularDto identifiable) {
        RegularEntity entity = new RegularEntity(identifiable.getAttributeString(), identifiable.getAttributeBoolean(),
                identifiable.getAttributeLong(), identifiable.getAttributeDate(), identifiable.getAttributeEnum(),
                identifiable.getAttributeCustomValueType());
        entity.setValueTypeCollection(identifiable.getValueTypeCollection());
        entity.setCustomValueTypeCollection(identifiable.getCustomValueTypeCollection());
        entity.setValueTypeMap(identifiable.getValueTypeMap());
        entity.setCustomValueTypeMap(identifiable.getCustomValueTypeMap());

        return entity;
    }

    @Override
    protected RegularDto doConvert(RegularEntity source) {
        RegularDto target = new RegularDto(Id.<RegularDto, Long> valueOf(source.getId()), source.getAttributeString(),
            source.getAttributeBoolean(), source.getAttributeLong(), source.getAttributeDate(),
                source.getAttributeEnum(), source.getAttributeCustomValueType());
        target.setValueTypeCollection(source.getValueTypeCollection());
        target.setCustomValueTypeCollection(source.getCustomValueTypeCollection());
        target.setValueTypeMap(source.getValueTypeMap());
        target.setCustomValueTypeMap(source.getCustomValueTypeMap());

        return target;
    }

    @Override
    protected void conform(RegularEntity target, RegularDto source) {
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
        target.setAttributeCustomValueType(source.getAttributeCustomValueType());

        target.setValueTypeCollection(source.getValueTypeCollection());
        target.setCustomValueTypeCollection(source.getCustomValueTypeCollection());
        target.setValueTypeMap(source.getValueTypeMap());
        target.setCustomValueTypeMap(source.getCustomValueTypeMap());
    }

    @Override
    protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder, Root<RegularEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<RegularEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                RegularEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeBooleanCondition(), RegularEntity_.attributeBoolean, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), RegularEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        RegularEntity_.attributeDate, builder, root));
            }

            if (filter.getAttributeDateConditionAsTimePeriod() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                        filter.getAttributeDateConditionAsTimePeriod(), RegularEntity_.attributeDate, builder, root));
            }

            if (filter.getAttributeLongConditionAsId() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicateForId(
                        filter.getAttributeLongConditionAsId(), RegularEntity_.attributeLong, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), RegularEntity_.attributeEnum, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super RegularEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return RegularEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return RegularEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return RegularEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return RegularEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return RegularEntity_.attributeEnum;
        }
        return null;
    }
}
