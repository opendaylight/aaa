/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.customid;

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
public class CustomIdDao extends JpaOffsetPageDao<String, CustomIdDto, CustomIdEntity, FilterMock, SortKeyMock> {

    public CustomIdDao() {
        super(CustomIdEntity.class);
    }

    @Override
    protected String getId(CustomIdEntity entity) {
        return entity.getId();
    }

    @Override
    protected CustomIdEntity create(CustomIdDto identifiable) {
        return new CustomIdEntity(identifiable.getId().getValue(), identifiable.getAttributeString(),
                identifiable.getAttributeBoolean(), identifiable.getAttributeLong(), identifiable.getAttributeDate(),
                identifiable.getAttributeEnum());
    }

    @Override
    protected CustomIdDto doConvert(CustomIdEntity source) {
        return new CustomIdDto(Id.<CustomIdDto, String> valueOf(source.getId()), source.getAttributeString(),
            source.getAttributeBoolean(), source.getAttributeLong(), source.getAttributeDate(),
            source.getAttributeEnum());
    }

    @Override
    protected void conform(CustomIdEntity target, CustomIdDto source) {
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
    }

    @Override
    protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder, Root<CustomIdEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<CustomIdEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                    CustomIdEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeBooleanCondition(), CustomIdEntity_.attributeBoolean, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), CustomIdEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        CustomIdEntity_.attributeDate, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), CustomIdEntity_.attributeEnum, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super CustomIdEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return CustomIdEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return CustomIdEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return CustomIdEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return CustomIdEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return CustomIdEntity_.attributeEnum;
        }
        return null;
    }
}
