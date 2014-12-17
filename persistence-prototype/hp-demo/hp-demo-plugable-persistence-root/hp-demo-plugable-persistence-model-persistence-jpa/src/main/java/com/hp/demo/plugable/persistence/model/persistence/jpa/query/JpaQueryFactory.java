/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.persistence.jpa.query;

import java.util.List;

import com.hp.demo.plugable.persistence.common.model.NetworkDevice;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceSortKey;
import com.hp.demo.plugable.persistence.common.model.User;
import com.hp.demo.plugable.persistence.common.model.UserFilter;
import com.hp.demo.plugable.persistence.model.persistence.api.QueryFactory;
import com.hp.demo.plugable.persistence.model.persistence.jpa.dao.NetworkDeviceDao;
import com.hp.demo.plugable.persistence.model.persistence.jpa.dao.UserDao;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.auth.Username;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.query.DeleteByIdQuery;
import com.hp.util.model.persistence.query.FindQuery;
import com.hp.util.model.persistence.query.GetQuery;
import com.hp.util.model.persistence.query.StoreQuery;
import com.hp.util.model.persistence.query.VoidQuery;

/**
 * JPA {@link QueryFactory}.
 * 
 * @author Fabiel Zuniga
 */
public class JpaQueryFactory implements QueryFactory<JpaContext> {

    /*
     * Custom queries should be implemented in this package and they should be package private. In
     * fact, everything in this module but this class could be package private: Entities, DAOs and
     * custom queries. This query factory is the only API the Business logic should interact with.
     * However, that would require everything to be contained in a single package. Thus, even though
     * entities and DAOs are not supposed to be used by anyone but this class, it was decided to
     * locate them in different packages for code organization purposes.
     */

    private final ConfigurationFactory<JpaContext> configurationFactory;
    private final NetworkDeviceFactory<JpaContext> networkDeviceFactory;
    private final UserFactory<JpaContext> userFactory;

    /**
     * Creates a query factory.
     */
    public JpaQueryFactory() {
        this.configurationFactory = new ConfigurationFactoryImpl();
        this.networkDeviceFactory = new NetworkDeviceFactoryImpl();
        this.userFactory = new UserFactoryImpl();
    }

    @Override
    public ConfigurationFactory<JpaContext> configuration() {
        return this.configurationFactory;
    }

    @Override
    public NetworkDeviceFactory<JpaContext> networkDevice() {
        return this.networkDeviceFactory;
    }

    @Override
    public UserFactory<JpaContext> user() {
        return this.userFactory;
    }

    private static class ConfigurationFactoryImpl implements ConfigurationFactory<JpaContext> {

        @Override
        public Query<Void, JpaContext> createSchema() {
            return VoidQuery.getInstance();
        }
    }

    private static class NetworkDeviceFactoryImpl implements NetworkDeviceFactory<JpaContext> {
        private final NetworkDeviceDao dao;

        public NetworkDeviceFactoryImpl() {
            this.dao = new NetworkDeviceDao();
        }

        @Override
        public Query<Void, JpaContext> store(NetworkDevice device) {
            return StoreQuery.createQuery(device, this.dao);
        }

        @Override
        public Query<NetworkDevice, JpaContext> get(Id<NetworkDevice, SerialNumber> id) {
            return GetQuery.createQuery(id, this.dao);
        }

        @Override
        public Query<List<NetworkDevice>, JpaContext> find(NetworkDeviceFilter filter,
                SortSpecification<NetworkDeviceSortKey> sortSpecification) {
            return FindQuery.createQuery(filter, sortSpecification, this.dao);
        }

        @Override
        public Query<Void, JpaContext> delete(Id<NetworkDevice, SerialNumber> id) {
            return DeleteByIdQuery.createQuery(id, this.dao);
        }
    }

    private static class UserFactoryImpl implements UserFactory<JpaContext> {
        private final UserDao dao;

        public UserFactoryImpl() {
            this.dao = new UserDao();
        }

        @Override
        public Query<Void, JpaContext> store(User user) {
            return StoreQuery.createQuery(user, this.dao);
        }

        @Override
        public Query<User, JpaContext> get(Id<User, Username> id) {
            return GetQuery.createQuery(id, this.dao);
        }

        @Override
        public Query<List<User>, JpaContext> find(UserFilter filter) {
            return FindQuery.createQuery(filter, null, this.dao);
        }

        @Override
        public Query<Void, JpaContext> delete(Id<User, Username> id) {
            return DeleteByIdQuery.createQuery(id, this.dao);
        }
    }
}
