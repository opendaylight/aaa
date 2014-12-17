/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.persistence.cassandra.query;

import java.util.List;

import com.hp.demo.plugable.persistence.common.model.NetworkDevice;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceSortKey;
import com.hp.demo.plugable.persistence.common.model.User;
import com.hp.demo.plugable.persistence.common.model.UserFilter;
import com.hp.demo.plugable.persistence.model.persistence.api.QueryFactory;
import com.hp.demo.plugable.persistence.model.persistence.cassandra.dao.NetworkDeviceDao;
import com.hp.demo.plugable.persistence.model.persistence.cassandra.dao.UserDao;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.auth.Username;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.query.DeleteByIdQuery;
import com.hp.util.model.persistence.query.FindQuery;
import com.hp.util.model.persistence.query.GetQuery;
import com.hp.util.model.persistence.query.StoreQuery;

/**
 * Cassandra {@link QueryFactory}.
 * <p>
 * Queries implementations in this factory do not use any of the native features of the Cassandra
 * Client, so it can be parameterized with any Native Cassandra Client.
 * 
 * @param <N> type of the native Cassandra client
 * @author Fabiel Zuniga
 */
public class CassandraQueryFactory<N> implements QueryFactory<CassandraContext<N>> {

    /*
     * Custom queries should be implemented in this package and they should be package private. In
     * fact, everything in this module but this class could be package private: Entities, DAOs and
     * custom queries. This query factory is the only API the Business logic should interact with.
     * However, that would require everything to be contained in a single package. Thus, even though
     * entities and DAOs are not supposed to be used by anyone but this class, it was decided to
     * locate them in different packages for code organization purposes.
     */

    private final ConfigurationFactory<CassandraContext<N>> configurationFactory;
    private final NetworkDeviceFactory<CassandraContext<N>> networkDeviceFactory;
    private final UserFactory<CassandraContext<N>> userFactory;

    /**
     * Creates a query factory.
     */
    public CassandraQueryFactory() {
        this.configurationFactory = new ConfigurationFactoryImpl<N>();
        this.networkDeviceFactory = new NetworkDeviceFactoryImpl<N>();
        this.userFactory = new UserFactoryImpl<N>();
    }

    @Override
    public ConfigurationFactory<CassandraContext<N>> configuration() {
        return this.configurationFactory;
    }

    @Override
    public NetworkDeviceFactory<CassandraContext<N>> networkDevice() {
        return this.networkDeviceFactory;
    }

    @Override
    public UserFactory<CassandraContext<N>> user() {
        return this.userFactory;
    }

    private static class ConfigurationFactoryImpl<N> implements ConfigurationFactory<CassandraContext<N>> {

        @Override
        public Query<Void, CassandraContext<N>> createSchema() {
            return new CreateSchemaQuery<N>();
        }
    }

    private static class NetworkDeviceFactoryImpl<N> implements NetworkDeviceFactory<CassandraContext<N>> {
        private final NetworkDeviceDao<N> dao;

        public NetworkDeviceFactoryImpl() {
            this.dao = new NetworkDeviceDao<N>();
        }

        @Override
        public Query<Void, CassandraContext<N>> store(NetworkDevice device) {
            return StoreQuery.createQuery(device, this.dao);
        }

        @Override
        public Query<NetworkDevice, CassandraContext<N>> get(Id<NetworkDevice, SerialNumber> id) {
            return GetQuery.createQuery(id, this.dao);
        }

        @Override
        public Query<List<NetworkDevice>, CassandraContext<N>> find(NetworkDeviceFilter filter,
                SortSpecification<NetworkDeviceSortKey> sortSpecification) {
            return FindQuery.createQuery(filter, sortSpecification, this.dao);
        }

        @Override
        public Query<Void, CassandraContext<N>> delete(Id<NetworkDevice, SerialNumber> id) {
            return DeleteByIdQuery.createQuery(id, this.dao);
        }
    }

    private static class UserFactoryImpl<N> implements UserFactory<CassandraContext<N>> {
        private final UserDao<N> dao;

        public UserFactoryImpl() {
            this.dao = new UserDao<N>();
        }

        @Override
        public Query<Void, CassandraContext<N>> store(User user) {
            return StoreQuery.createQuery(user, this.dao);
        }

        @Override
        public Query<User, CassandraContext<N>> get(Id<User, Username> id) {
            return GetQuery.createQuery(id, this.dao);
        }

        @Override
        public Query<List<User>, CassandraContext<N>> find(UserFilter filter) {
            return FindQuery.createQuery(filter, null, this.dao);
        }

        @Override
        public Query<Void, CassandraContext<N>> delete(Id<User, Username> id) {
            return DeleteByIdQuery.createQuery(id, this.dao);
        }
    }
}
