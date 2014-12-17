/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.dependent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.converter.DateConverter;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.dao.VersionedUpdateStrategy;
import com.hp.util.model.persistence.jpa.dao.JpaOffsetPageDependentDao;
import com.hp.util.model.persistence.jpa.dao.JpaQueryPredicateGenerator;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class DependentDao
        extends
        JpaOffsetPageDependentDao<Long, DependentDto, DependentEntity, FilterMock, SortKeyMock, Long, IndependentDto, IndependentEntity> {

    public DependentDao() {
        super(DependentEntity.class, new VersionedUpdateStrategy<DependentEntity, DependentDto>());
    }

    @Override
    protected Long getId(DependentEntity entity) {
        return entity.getId();
    }

    @Override
    public DependentEntity create(DependentDto identifiable, IndependentEntity owner) {
        return new DependentEntity(owner, identifiable.getNaturalKey(), identifiable.getAttributeString(),
                identifiable.getAttributeBoolean(), identifiable.getAttributeLong(), identifiable.getAttributeDate(),
                identifiable.getAttributeEnum());
    }

    @Override
    public void conform(DependentEntity target, DependentDto source) {
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
    }

    @Override
    protected DependentDto doConvert(DependentEntity source) {
        Id<DependentDto, Long> id = Id.valueOf(source.getId());
        Id<IndependentDto, Long> ownerId = Id.valueOf(source.getOwnerId());
        DependentDto dependent = new DependentDto(id, ownerId, source.getNaturalKey(),
                source.getAttributeString(), source.getAttributeBoolean(), source.getAttributeLong(),
                source.getAttributeDate(), source.getAttributeEnum());
        dependent.setVersion(source.getVersion());
        return dependent;

    }

    @Override
    protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder, Root<DependentEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<DependentEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                    DependentEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeBooleanCondition(), DependentEntity_.attributeBoolean, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), DependentEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        DependentEntity_.attributeDate, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), DependentEntity_.attributeEnum, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super DependentEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return DependentEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return DependentEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return DependentEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return DependentEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return DependentEntity_.attributeEnum;
        }
        return null;
    }
}
