/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.versioned;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.converter.DateConverter;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.dao.VersionedUpdateStrategy;
import com.hp.util.model.persistence.jpa.dao.JpaOffsetPageDao;
import com.hp.util.model.persistence.jpa.dao.JpaQueryPredicateGenerator;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;

/**
 * Versioned DAO mock.
 * 
 * @author Fabiel Zuniga
 */
public class VersionedDao extends JpaOffsetPageDao<Long, VersionedDto, VersionedEntity, FilterMock, SortKeyMock> {

    /**
     * Creates a DAO.
     */
    public VersionedDao() {
        super(VersionedEntity.class, new VersionedUpdateStrategy<VersionedEntity, VersionedDto>());
    }

    @Override
    protected Long getId(VersionedEntity entity) {
        return entity.getId();
    }

    @Override
    protected VersionedEntity create(VersionedDto identifiable) {
        return new VersionedEntity(identifiable.getAttributeString(), identifiable.getAttributeBoolean(),
                identifiable.getAttributeLong(), identifiable.getAttributeDate(), identifiable.getAttributeEnum());
    }

    @Override
    protected void conform(VersionedEntity target, VersionedDto source) {
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
    }

    @Override
    protected VersionedDto doConvert(VersionedEntity source) {
        VersionedDto versioned = new VersionedDto(Id.<VersionedDto, Long> valueOf(source.getId()),
                source.getAttributeString(), source.getAttributeBoolean(), source.getAttributeLong(),
                source.getAttributeDate(), source.getAttributeEnum());
        versioned.setVersion(source.getVersion());
        return versioned;
    }

    @Override
    protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder, Root<VersionedEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<VersionedEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                VersionedEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeBooleanCondition(), VersionedEntity_.attributeBoolean, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), VersionedEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        VersionedEntity_.attributeDate, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), VersionedEntity_.attributeEnum, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<VersionedEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return VersionedEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return VersionedEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return VersionedEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return VersionedEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return VersionedEntity_.attributeEnum;
        }
        return null;
    }
}
