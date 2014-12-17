/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.direct.regular;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.util.common.converter.DateConverter;
import com.hp.util.model.persistence.jpa.dao.JpaOffsetPageDaoDirect;
import com.hp.util.model.persistence.jpa.dao.JpaQueryPredicateGenerator;
import com.hp.util.model.persistence.jpa.dao.mock.FilterMock;
import com.hp.util.model.persistence.jpa.dao.mock.SortKeyMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class RegularDirectDao extends JpaOffsetPageDaoDirect<Long, RegularDirectEntity, FilterMock, SortKeyMock> {

    public RegularDirectDao() {
        super(RegularDirectEntity.class);
    }

    @Override
    protected Predicate getQueryPredicate(FilterMock filter, CriteriaBuilder builder, Root<RegularDirectEntity> root) {
        Predicate predicate = null;

        if (filter != null) {
            JpaQueryPredicateGenerator<RegularDirectEntity> predicateGenerator = getQueryPredicateGenerator();

            predicate = predicateGenerator.getPredicate(filter.getAttributeStringCondition(),
                    RegularDirectEntity_.attributeString, builder, root);

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeBooleanCondition(), RegularDirectEntity_.attributeBoolean, builder, root));

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeLongCondition(), RegularDirectEntity_.attributeLong, builder, root));

            if (filter.getAttributeDateCondition() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(filter
                        .getAttributeDateCondition().convert(DateConverter.getInstance()),
                        RegularDirectEntity_.attributeDate, builder, root));
            }

            if (filter.getAttributeDateConditionAsTimePeriod() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                        filter.getAttributeDateConditionAsTimePeriod(), RegularDirectEntity_.attributeDate, builder,
                        root));
            }

            if (filter.getAttributeLongConditionAsId() != null) {
                predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicateForId(
                        filter.getAttributeLongConditionAsId(), RegularDirectEntity_.attributeLong, builder, root));
            }

            predicate = predicateGenerator.and(builder, predicate, predicateGenerator.getPredicate(
                    filter.getAttributeEnumCondition(), RegularDirectEntity_.attributeEnum, builder, root));
        }

        return predicate;
    }

    @Override
    protected SingularAttribute<? super RegularDirectEntity, ?> getSingularAttribute(SortKeyMock sortKey) {
        switch (sortKey) {
            case STRING_ATTRIBUTE:
                return RegularDirectEntity_.attributeString;
            case BOOLEAN_ATTRIBUTE:
                return RegularDirectEntity_.attributeBoolean;
            case LONG_ATTRIBUTE:
                return RegularDirectEntity_.attributeLong;
            case DATE_ATTRIBUTE:
                return RegularDirectEntity_.attributeDate;
            case ENUM_ATTRIBUTE:
                return RegularDirectEntity_.attributeEnum;
        }
        return null;
    }
}
