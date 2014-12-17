/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.compositeid.preferred;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.converter.DateConverter;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.jpa.dao.JpaMappedKeyDao;
import com.hp.util.model.persistence.jpa.dao.JpaQueryPredicateGenerator;
import com.hp.util.model.persistence.jpa.dao.compositeid.preferred.CompositeIdPreferredEntity.EmbeddableCompositeId;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class CompositeIdDao extends
        JpaMappedKeyDao<CompositeId, CompositeIdDto, EmbeddableCompositeId, CompositeIdPreferredEntity, FilterMock, SortKeyMock> {

    public CompositeIdDao() {
        super(CompositeIdPreferredEntity.class);
    }

    @Override
    protected CompositeId getId(CompositeIdPreferredEntity entity) {
        return entity.getId();
    }

    @Override
    protected CompositeIdPreferredEntity create(CompositeIdDto Identifiable) {
        return new CompositeIdPreferredEntity(Identifiable.getId().getValue(), Identifiable.getAttributeString(),
                Identifiable.getAttributeBoolean(), Identifiable.getAttributeLong(), Identifiable.getAttributeDate(),
                Identifiable.getAttributeEnum());
    }

    @Override
    protected CompositeIdDto doConvert(CompositeIdPreferredEntity source) {
        return new CompositeIdDto(Id.<CompositeIdDto, CompositeId> valueOf(source.getId()),
            source.getAttributeString(), source.getAttributeBoolean(), source.getAttributeLong(),
            source.getAttributeDate(), source.getAttributeEnum());
    }

    @Override
    protected void conform(CompositeIdPreferredEntity target, CompositeIdDto source) {
        target.setAttributeString(source.getAttributeString());
        target.setAttributeBoolean(source.getAttributeBoolean());
        target.setAttributeLong(source.getAttributeLong());
        target.setAttributeDate(source.getAttributeDate());
        target.setAttributeEnum(source.getAttributeEnum());
    }

    @Override
    protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder, Root<CompositeIdPreferredEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<CompositeIdPreferredEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                    CompositeIdPreferredEntity_.attributeString, builder, root);

            predicate = predicateGenerator
                    .and(builder, predicate, predicateGenerator.getPredicate(filter.getAttributeBooleanCondition(),
                            CompositeIdPreferredEntity_.attributeBoolean, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), CompositeIdPreferredEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        CompositeIdPreferredEntity_.attributeDate, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), CompositeIdPreferredEntity_.attributeEnum, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super CompositeIdPreferredEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return CompositeIdPreferredEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return CompositeIdPreferredEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return CompositeIdPreferredEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return CompositeIdPreferredEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return CompositeIdPreferredEntity_.attributeEnum;
        }
        return null;
    }

    @Override
    protected EmbeddableCompositeId mapKey(CompositeId key) {
        return new EmbeddableCompositeId(key);
    }
}
