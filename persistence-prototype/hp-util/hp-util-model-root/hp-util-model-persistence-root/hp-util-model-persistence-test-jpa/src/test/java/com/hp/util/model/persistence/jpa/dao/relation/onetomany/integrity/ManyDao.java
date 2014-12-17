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
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class ManyDao extends JpaOffsetPageDao<Long, ManyDto, OneToManyIntegrityManyEntity, ManyFilterMock, SortKeyMock> {

    private OneDao relativeDao;

    protected ManyDao(OneDao relativeDao) {
        super(OneToManyIntegrityManyEntity.class);

        if (relativeDao == null) {
            throw new NullPointerException("relativeDao cannot be null");
        }

        this.relativeDao = relativeDao;
    }

    @Override
    protected Long getId(OneToManyIntegrityManyEntity entity) {
        return entity.getId();
    }

    @Override
    public ManyDto create(ManyDto identifiable, JpaContext context) throws PersistenceException {
        if (identifiable == null) {
            throw new NullPointerException("identifiable cannot be null");
        }

        OneToManyIntegrityManyEntity entity = create(identifiable);

        //-------
        // Create method is override to add the relative.
        if (identifiable.getRelative() != null) {
            OneToManyIntegrityOneEntity relative = this.relativeDao.getEntity(identifiable.getRelative(), context);

            if (relative == null) {
                throw new IllegalArgumentException("Relative with id " + identifiable.getRelative() + " not found.");
            }

            entity.setRelative(relative);
        }
        //-------

        context.getEntityManager().persist(entity);
        return convert(entity);
    }

    @Override
    protected OneToManyIntegrityManyEntity create(ManyDto identifiable) {
        return new OneToManyIntegrityManyEntity(identifiable.getAttributeString(), identifiable.getAttributeBoolean(),
                identifiable.getAttributeLong(), identifiable.getAttributeDate(), identifiable.getAttributeEnum());
    }

    @Override
    public void update(ManyDto identifiable, JpaContext context) throws PersistenceException {
        if (identifiable == null) {
            throw new NullPointerException("identifiable cannot be null");
        }

        OneToManyIntegrityManyEntity entity = getEntity(identifiable.getId(), context);

        //-------
        // Update method is override to update the relative.
        if (identifiable.getRelative() != null) {
            if (!identifiable.getRelative().equals(Id.<OneDto, Long> valueOf(entity.getRelative().getId()))) {
                OneToManyIntegrityOneEntity newRelative = this.relativeDao.getEntity(identifiable.getRelative(),
                        context);
                if (newRelative == null) {
                    throw new IllegalArgumentException("Relative with id " + identifiable.getRelative()
                        + " not found.");
                }
                entity.setRelative(newRelative);
            }
        }
        else {
            entity.setRelative(null);
        }
        //-------

        if (getUpdateStrategy() != null) {
            getUpdateStrategy().validateWrite(entity, identifiable);
        }

        conform(entity, identifiable);
    }

    @Override
    protected void conform(OneToManyIntegrityManyEntity target, ManyDto source) {
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
    }

    @Override
    protected ManyDto doConvert(OneToManyIntegrityManyEntity source) {
        Id<OneDto, Long> relativeId = null;
        if (source.getRelative() != null) {
            relativeId = Id.valueOf(source.getRelative().getId());
        }

        return new ManyDto(Id.<ManyDto, Long> valueOf(source.getId()), source.getAttributeString(),
            source.getAttributeBoolean(), source.getAttributeLong(), source.getAttributeDate(),
            source.getAttributeEnum(), relativeId);
    }

    @Override
    protected Predicate getQueryPredicate(ManyFilterMock filter, CriteriaBuilder builder,
        Root<OneToManyIntegrityManyEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<OneToManyIntegrityManyEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                OneToManyIntegrityManyEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeBooleanCondition(), OneToManyIntegrityManyEntity_.attributeBoolean, builder,
                    root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), OneToManyIntegrityManyEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        OneToManyIntegrityManyEntity_.attributeDate, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), OneToManyIntegrityManyEntity_.attributeEnum, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicateForRelatedEntity(
                    filter.getRelativeCondition(), OneToManyIntegrityManyEntity_.relative, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super OneToManyIntegrityManyEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return OneToManyIntegrityManyEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return OneToManyIntegrityManyEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return OneToManyIntegrityManyEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return OneToManyIntegrityManyEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return OneToManyIntegrityManyEntity_.attributeEnum;
        }
        return null;
    }
}
