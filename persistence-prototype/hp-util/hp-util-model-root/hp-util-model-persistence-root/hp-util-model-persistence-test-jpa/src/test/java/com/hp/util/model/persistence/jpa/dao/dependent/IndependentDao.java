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

import com.hp.util.common.Provider;
import com.hp.util.common.converter.DateConverter;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.dao.DependentUpdater;
import com.hp.util.model.persistence.dao.DependentUpdater.Delegate;
import com.hp.util.model.persistence.dao.VersionedUpdateStrategy;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.dao.JpaOffsetPageDao;
import com.hp.util.model.persistence.jpa.dao.JpaQueryPredicateGenerator;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class IndependentDao extends JpaOffsetPageDao<Long, IndependentDto, IndependentEntity, FilterMock, SortKeyMock> {

    /*
     * Note that the dependent DAO is not needed for one-to-many unidirectional relations. The same
     * independent DAO can create dependent entities.
     */
    private DependentDao dependentDao;

    public IndependentDao(DependentDao dependentDao) {
        super(IndependentEntity.class, new VersionedUpdateStrategy<IndependentEntity, IndependentDto>());
        this.dependentDao = dependentDao;
    }

    @Override
    protected Long getId(IndependentEntity entity) {
        return entity.getId();
    }

    @Override
    public IndependentDto create(IndependentDto identifiable, JpaContext context) throws PersistenceException {
        IndependentDto independentDto = super.create(identifiable, context);
        IndependentEntity independentEntity = getEntity(independentDto.getId(), context);
        for (DependentDto dependent : identifiable.getDependents()) {
            DependentEntity dependentEntity = this.dependentDao.create(dependent, independentEntity);
            context.getEntityManager().persist(dependentEntity);
            independentEntity.addDependent(dependentEntity);
        }
        return convert(independentEntity);
    }

    @Override
    protected IndependentEntity create(IndependentDto identifiable) {
        return new IndependentEntity(identifiable.getAttributeString(), identifiable.getAttributeBoolean(),
                identifiable.getAttributeLong(), identifiable.getAttributeDate(), identifiable.getAttributeEnum());
    }

    @Override
    public void update(IndependentDto identifiable, final JpaContext context) throws PersistenceException {
        super.update(identifiable, context);

        final IndependentEntity independentEntity = getEntity(identifiable.getId(), context);

        Delegate<DependentEntity, DependentDto> delegate = new Delegate<DependentEntity, DependentDto>() {
            @Override
            public void add(DependentDto dependent) throws PersistenceException {
                DependentEntity dependentEntity = IndependentDao.this.dependentDao.create(dependent, independentEntity);
                context.getEntityManager().persist(dependentEntity);
                independentEntity.addDependent(dependentEntity);
            }

            @Override
            public void update(DependentEntity dependentTarget, DependentDto dependentSource)
                    throws PersistenceException {
                IndependentDao.this.dependentDao.conform(dependentTarget, dependentSource);
            }

            @Override
            public void delete(DependentEntity dependent) {
                independentEntity.removeDependent(dependent);
            }
        };

        Provider<Long, DependentEntity> idProvider = new Provider<Long, DependentEntity>() {
            @Override
            public Long get(DependentEntity entity) {
                return entity.getId();
            }
        };

        DependentUpdater.updateDependents(independentEntity.getDependents(), identifiable.getDependents(), idProvider,
                delegate);
    }

    @Override
    protected void conform(final IndependentEntity target, IndependentDto source) {
        // No need to conform dependents, they are handled in the override update method.
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
    }

    @Override
    protected IndependentDto doConvert(IndependentEntity source) {
        IndependentDto independent = new IndependentDto(Id.<IndependentDto, Long> valueOf(source.getId()),
                source.getAttributeString(), source.getAttributeBoolean(), source.getAttributeLong(),
                source.getAttributeDate(), source.getAttributeEnum());

        for (DependentEntity dependentEntity : source.getDependents()) {
            independent.addDependent(this.dependentDao.convert(dependentEntity));
        }

        independent.setVersion(source.getVersion());

        return independent;
    }

    @Override
    protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder, Root<IndependentEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<IndependentEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                IndependentEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeBooleanCondition(), IndependentEntity_.attributeBoolean, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), IndependentEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        IndependentEntity_.attributeDate, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), IndependentEntity_.attributeEnum, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super IndependentEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return IndependentEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return IndependentEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return IndependentEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return IndependentEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return IndependentEntity_.attributeEnum;
        }
        return null;
    }
}
