/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.compositeid;

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
public class CompositeIdDao extends
        JpaOffsetPageDao<CompositeId, CompositeIdDto, CompositeIdEntity, FilterMock, SortKeyMock> {

    public CompositeIdDao() {
        super(CompositeIdEntity.class);
    }

    @Override
    protected CompositeId getId(CompositeIdEntity entity) {
        return entity.getId();
    }

    @Override
    protected CompositeIdEntity create(CompositeIdDto identifiable) {
        return new CompositeIdEntity(identifiable.getId().getValue(), identifiable.getAttributeString(),
                identifiable.getAttributeBoolean(), identifiable.getAttributeLong(), identifiable.getAttributeDate(),
                identifiable.getAttributeEnum());
    }

    @Override
    protected CompositeIdDto doConvert(CompositeIdEntity source) {
        return new CompositeIdDto(Id.<CompositeIdDto, CompositeId> valueOf(source.getId()),
            source.getAttributeString(), source.getAttributeBoolean(), source.getAttributeLong(),
            source.getAttributeDate(), source.getAttributeEnum());
    }

    @Override
    protected void conform(CompositeIdEntity target, CompositeIdDto source) {
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
    }

    @Override
    protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder, Root<CompositeIdEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<CompositeIdEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                    CompositeIdEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeBooleanCondition(), CompositeIdEntity_.attributeBoolean, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), CompositeIdEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        CompositeIdEntity_.attributeDate, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), CompositeIdEntity_.attributeEnum, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super CompositeIdEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return CompositeIdEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return CompositeIdEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return CompositeIdEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return CompositeIdEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return CompositeIdEntity_.attributeEnum;
        }
        return null;
    }
}
