/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.persistence.jpa.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import com.hp.demo.plugable.persistence.common.model.User;
import com.hp.demo.plugable.persistence.common.model.UserFilter;
import com.hp.demo.plugable.persistence.common.model.UserFilter.All;
import com.hp.demo.plugable.persistence.common.model.UserFilter.ByEnabledStatus;
import com.hp.demo.plugable.persistence.model.persistence.jpa.entity.UserEntity;
import com.hp.demo.plugable.persistence.model.persistence.jpa.entity.UserEntity_;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.auth.Username;
import com.hp.util.model.persistence.jpa.dao.JpaMappedKeyDao;

/**
 * User DAO.
 * 
 * @author Fabiel Zuniga
 */
public class UserDao extends JpaMappedKeyDao<Username, User, String, UserEntity, UserFilter, Void> {

    /**
     * Creates a DAO.
     */
    public UserDao() {
        super(UserEntity.class);
    }

    @Override
    protected String mapKey(Username key) {
        return key.getValue();
    }

    @Override
    protected Username getId(UserEntity entity) {
        return entity.getId();
    }

    @Override
    protected UserEntity create(User user) {
        UserEntity entity = new UserEntity(user.getId().getValue());
        entity.setPassword(user.getPassword());
        entity.setEmail(user.getEmail());
        entity.setDescription(user.getDescription());
        entity.setEnabled(user.isEnabled());
        return entity;
    }

    @Override
    protected void conform(UserEntity target, User source) {
        target.setPassword(source.getPassword());
        target.setEmail(source.getEmail());
        target.setDescription(source.getDescription());
        target.setEnabled(source.isEnabled());
    }

    @Override
    protected User doConvert(UserEntity source) {
        Id<User, Username> id = Id.valueOf(source.getId());
        User user = new User(id);
        user.setPassword(source.getPassword());
        user.setEmail(source.getEmail());
        user.setDescription(source.getDescription());
        user.setEnabled(source.isEnabled());
        return user;
    }

    @Override
    protected Predicate getQueryPredicate(UserFilter userFilter, final CriteriaBuilder builder,
            final Root<UserEntity> root) {
        UserFilter.Visitor<Predicate> visitor = new UserFilter.Visitor<Predicate>() {

            @Override
            public Predicate visit(All filter) {
                return null;
            }

            @Override
            public Predicate visit(ByEnabledStatus filter) {
                return getQueryPredicateGenerator().getPredicate(filter.getEnabledStatusCondition(),
                        UserEntity_.enabled, builder, root);
            }
        };

        return nonnull(userFilter).accept(visitor);
    }

    @Override
    protected SingularAttribute<? super UserEntity, ?> getSingularAttribute(Void sortKey) {
        return null;
    }

    private static UserFilter nonnull(UserFilter filter) {
        if (filter != null) {
            return filter;
        }
        return UserFilter.filterAll();
    }
}
