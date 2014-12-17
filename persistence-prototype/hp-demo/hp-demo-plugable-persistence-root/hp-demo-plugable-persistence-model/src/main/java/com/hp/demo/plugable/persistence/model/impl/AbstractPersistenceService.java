/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.impl;

import java.util.List;

import com.hp.demo.plugable.persistence.common.model.NetworkDevice;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceSortKey;
import com.hp.demo.plugable.persistence.common.model.User;
import com.hp.demo.plugable.persistence.common.model.UserFilter;
import com.hp.demo.plugable.persistence.model.PersistenceService;
import com.hp.demo.plugable.persistence.model.persistence.api.QueryFactory;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.auth.Username;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;

/**
 * @author Fabiel Zuniga
 */
abstract class AbstractPersistenceService<C> implements PersistenceService {

    private final DataStore<C> dataStore;
    private final QueryFactory<C> queryFactory;
    
    private final NetworkDevicePersistenceService networkDevicePersistenceService;
    private final UserPersistenceService userPersistenceService;

    public AbstractPersistenceService(DataStore<C> dataStore, QueryFactory<C> queryFactory) {
        if (dataStore == null) {
            throw new NullPointerException("dataStore cannot be null");
        }

        if (queryFactory == null) {
            throw new NullPointerException("queryFactory cannot be null");
        }

        this.dataStore = dataStore;
        this.queryFactory = queryFactory;
        this.networkDevicePersistenceService = new NetworkDevicePersistenceServiceImpl();
        this.userPersistenceService = new UserPersistenceServiceImpl();
    }

    @Override
    public NetworkDevicePersistenceService networkDevice() {
        return this.networkDevicePersistenceService;
    }

    @Override
    public UserPersistenceService user() {
        return this.userPersistenceService;
    }

    protected DataStore<C> getDataStore() {
        return this.dataStore;
    }

    protected QueryFactory<C> getQueryFactory() {
        return this.queryFactory;
    }

    private class NetworkDevicePersistenceServiceImpl implements NetworkDevicePersistenceService {

        @Override
        public void store(NetworkDevice device) throws PersistenceException {
            getDataStore().execute(getQueryFactory().networkDevice().store(device));
        }

        @Override
        public NetworkDevice get(Id<NetworkDevice, SerialNumber> id) throws PersistenceException {
            return getDataStore().execute(getQueryFactory().networkDevice().get(id));
        }

        @Override
        public List<NetworkDevice> find(NetworkDeviceFilter filter,
                SortSpecification<NetworkDeviceSortKey> sortSpecification) throws PersistenceException {
            return getDataStore().execute(getQueryFactory().networkDevice().find(filter, sortSpecification));
        }

        @Override
        public void delete(Id<NetworkDevice, SerialNumber> id) throws PersistenceException {
            getDataStore().execute(getQueryFactory().networkDevice().delete(id));
        }
    }

    private class UserPersistenceServiceImpl implements UserPersistenceService {

        @Override
        public void store(User user) throws PersistenceException {
            getDataStore().execute(getQueryFactory().user().store(user));
        }

        @Override
        public User get(Id<User, Username> id) throws PersistenceException {
            return getDataStore().execute(getQueryFactory().user().get(id));
        }

        @Override
        public List<User> find(UserFilter filter) throws PersistenceException {
            return getDataStore().execute(getQueryFactory().user().find(filter));
        }

        @Override
        public void delete(Id<User, Username> id) throws PersistenceException {
            getDataStore().execute(getQueryFactory().user().delete(id));
        }
    }
}
