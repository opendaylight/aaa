/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.relation.onetomany.nointegrity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.converter.DateConverter;
import com.hp.util.common.filter.EqualityCondition;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.dao.JpaDao;
import com.hp.util.model.persistence.jpa.dao.JpaOffsetPageDao;
import com.hp.util.model.persistence.jpa.dao.JpaQueryPredicateGenerator;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class OneDao extends JpaOffsetPageDao<Long, OneDto, OneToManyNoIntegrityOneEntity, FilterMock, SortKeyMock> {

    private JpaDao<?, ?, ?, ManyFilterMock, ?> relativeDao;

    public OneDao() {
        super(OneToManyNoIntegrityOneEntity.class);
    }

    public void setRelativeDao(JpaDao<?, ?, ?, ManyFilterMock, ?> relativeDao) {
        this.relativeDao = relativeDao;
    }

    @Override
    protected Long getId(OneToManyNoIntegrityOneEntity entity) {
        return entity.getId();
    }

    @Override
    protected OneToManyNoIntegrityOneEntity create(OneDto identifiable) {
        return new OneToManyNoIntegrityOneEntity(identifiable.getAttributeString(), identifiable.getAttributeBoolean(),
                identifiable.getAttributeLong(), identifiable.getAttributeDate(), identifiable.getAttributeEnum());
    }

    @Override
    protected OneDto doConvert(OneToManyNoIntegrityOneEntity source) {
        return new OneDto(Id.<OneDto, Long> valueOf(source.getId()), source.getAttributeString(),
            source.getAttributeBoolean(), source.getAttributeLong(), source.getAttributeDate(),
            source.getAttributeEnum());
    }

    @Override
    protected void conform(OneToManyNoIntegrityOneEntity target, OneDto source) {
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
    }

    @Override
    protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder,
        Root<OneToManyNoIntegrityOneEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<OneToManyNoIntegrityOneEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                OneToManyNoIntegrityOneEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeBooleanCondition(), OneToManyNoIntegrityOneEntity_.attributeBoolean, builder,
                    root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), OneToManyNoIntegrityOneEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        OneToManyNoIntegrityOneEntity_.attributeDate, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), OneToManyNoIntegrityOneEntity_.attributeEnum, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super OneToManyNoIntegrityOneEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return OneToManyNoIntegrityOneEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return OneToManyNoIntegrityOneEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return OneToManyNoIntegrityOneEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return OneToManyNoIntegrityOneEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return OneToManyNoIntegrityOneEntity_.attributeEnum;
        }
        return null;
    }

    @Override
    protected void deleteEntity(OneToManyNoIntegrityOneEntity entity, JpaContext context) throws PersistenceException {
        if (this.relativeDao == null) {
            throw new UnsupportedOperationException(
                "Delete operation is not allowed because relative DAO has not been set");
        }

        ManyFilterMock filter = new ManyFilterMock();
        Id<OneDto, Long> id = Id.valueOf(entity.getId());
        filter.setRelativeCondition(EqualityCondition.equalTo(id));
        if (this.relativeDao.count(filter, context) > 0) {
            throw new IllegalStateException(
                    "Delete operation is not allowed because there are ManyDto objects related to " + entity);
        }
        super.deleteEntity(entity, context);
    }

    @Override
    protected OneToManyNoIntegrityOneEntity getEntity(Id<OneDto, Long> id, JpaContext context)
            throws PersistenceException {
        return super.getEntity(id, context);
    }
}
