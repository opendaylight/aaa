/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.relation.onetomany.integrity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.converter.DateConverter;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.dao.JpaOffsetPageDao;
import com.hp.util.model.persistence.jpa.dao.JpaQueryPredicateGenerator;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class OneDao extends JpaOffsetPageDao<Long, OneDto, OneToManyIntegrityOneEntity, FilterMock, SortKeyMock> {

    public OneDao() {
        super(OneToManyIntegrityOneEntity.class);
    }

    @Override
    protected Long getId(OneToManyIntegrityOneEntity entity) {
        return entity.getId();
    }

    @Override
    protected OneToManyIntegrityOneEntity create(OneDto identifiable) {
        return new OneToManyIntegrityOneEntity(identifiable.getAttributeString(), identifiable.getAttributeBoolean(),
                identifiable.getAttributeLong(), identifiable.getAttributeDate(), identifiable.getAttributeEnum());
    }

    @Override
    protected OneDto doConvert(OneToManyIntegrityOneEntity source) {
        return new OneDto(Id.<OneDto, Long> valueOf(source.getId()), source.getAttributeString(),
            source.getAttributeBoolean(), source.getAttributeLong(), source.getAttributeDate(),
            source.getAttributeEnum());
    }

    @Override
    protected void conform(OneToManyIntegrityOneEntity target, OneDto source) {
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
    }

    @Override
    protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder,
        Root<OneToManyIntegrityOneEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<OneToManyIntegrityOneEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                OneToManyIntegrityOneEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate,
                    predicateGenerator.getPredicate(filter.getAttributeBooleanCondition(),
                            OneToManyIntegrityOneEntity_.attributeBoolean, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), OneToManyIntegrityOneEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        OneToManyIntegrityOneEntity_.attributeDate, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), OneToManyIntegrityOneEntity_.attributeEnum, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super OneToManyIntegrityOneEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return OneToManyIntegrityOneEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return OneToManyIntegrityOneEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return OneToManyIntegrityOneEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return OneToManyIntegrityOneEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return OneToManyIntegrityOneEntity_.attributeEnum;
        }
        return null;
    }

    @Override
    protected OneToManyIntegrityOneEntity getEntity(Id<OneDto, Long> id, JpaContext context)
            throws PersistenceException {
        return super.getEntity(id, context);
    }
}
