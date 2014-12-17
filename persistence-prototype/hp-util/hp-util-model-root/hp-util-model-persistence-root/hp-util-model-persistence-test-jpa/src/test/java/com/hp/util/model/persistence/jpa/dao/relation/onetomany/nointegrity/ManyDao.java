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
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.dao.JpaOffsetPageDao;
import com.hp.util.model.persistence.jpa.dao.JpaQueryPredicateGenerator;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class ManyDao extends
        JpaOffsetPageDao<Long, ManyDto, OneToManyNoIntegrityManyEntity, ManyFilterMock, SortKeyMock> {

    private OneDao relativeDao;

    protected ManyDao(OneDao relativeDao) {
        super(OneToManyNoIntegrityManyEntity.class);

        if (relativeDao == null) {
            throw new NullPointerException("relativeDao cannot be null");
        }

        this.relativeDao = relativeDao;
    }

    @Override
    protected Long getId(OneToManyNoIntegrityManyEntity entity) {
        return entity.getId();
    }

    @Override
    public ManyDto create(ManyDto identifiable, JpaContext context) throws PersistenceException {
        if (identifiable == null) {
            throw new NullPointerException("identifiable cannot be null");
        }

        OneToManyNoIntegrityManyEntity entity = create(identifiable);

        //-------
        // Create method is override to add the relative.
        if (identifiable.getRelative() != null) {
            OneToManyNoIntegrityOneEntity relative = this.relativeDao.getEntity(identifiable.getRelative(), context);

            if (relative == null) {
                throw new IllegalArgumentException("Relative with id " + identifiable.getRelative() + " not found.");
            }

            entity.setRelativeId(relative.getId());
        }
        //-------

        context.getEntityManager().persist(entity);
        return convert(entity);
    }

    @Override
    protected OneToManyNoIntegrityManyEntity create(ManyDto identifiable) {
        return new OneToManyNoIntegrityManyEntity(identifiable.getAttributeString(),
                identifiable.getAttributeBoolean(), identifiable.getAttributeLong(), identifiable.getAttributeDate(),
                identifiable.getAttributeEnum());
    }

    @Override
    public void update(ManyDto identifiable, JpaContext context) throws PersistenceException {
        if (identifiable == null) {
            throw new NullPointerException("identifiable cannot be null");
        }

        OneToManyNoIntegrityManyEntity entity = getEntity(identifiable.getId(), context);

        //-------
        // Create method is override to update the relative.
        if (identifiable.getRelative() != null) {
            if (!identifiable.getRelative().equals(Id.<OneDto, Long> valueOf(entity.getRelativeId()))) {
                OneToManyNoIntegrityOneEntity newRelative = this.relativeDao.getEntity(identifiable.getRelative(),
                        context);
                if (newRelative == null) {
                    throw new IllegalArgumentException("Relative with id " + identifiable.getRelative()
                        + " not found.");
                }
                entity.setRelativeId(newRelative.getId());
            }
        }
        else {
            entity.setRelativeId(null);
        }
        //-------

        if (getUpdateStrategy() != null) {
            getUpdateStrategy().validateWrite(entity, identifiable);
        }

        conform(entity, identifiable);
    }

    @Override
    protected void conform(OneToManyNoIntegrityManyEntity target, ManyDto source) {
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
    }

    @Override
    protected ManyDto doConvert(OneToManyNoIntegrityManyEntity source) {
        Id<OneDto, Long> relativeId = null;
        if (source.getRelativeId() != null) {
            relativeId = Id.valueOf(source.getRelativeId());
        }

        return new ManyDto(Id.<ManyDto, Long> valueOf(source.getId()), source.getAttributeString(),
            source.getAttributeBoolean(), source.getAttributeLong(), source.getAttributeDate(),
            source.getAttributeEnum(), relativeId);
    }

    @Override
    protected Predicate getQueryPredicate(ManyFilterMock filter, CriteriaBuilder builder,
        Root<OneToManyNoIntegrityManyEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<OneToManyNoIntegrityManyEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                OneToManyNoIntegrityManyEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeBooleanCondition(), OneToManyNoIntegrityManyEntity_.attributeBoolean, builder,
                    root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), OneToManyNoIntegrityManyEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        OneToManyNoIntegrityManyEntity_.attributeDate, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), OneToManyNoIntegrityManyEntity_.attributeEnum, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicateForId(
                    filter.getRelativeCondition(), OneToManyNoIntegrityManyEntity_.relativeId, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super OneToManyNoIntegrityManyEntity, ?> getSingularAttribute(
        SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return OneToManyNoIntegrityManyEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return OneToManyNoIntegrityManyEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return OneToManyNoIntegrityManyEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return OneToManyNoIntegrityManyEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return OneToManyNoIntegrityManyEntity_.attributeEnum;
        }
        return null;
    }
}
