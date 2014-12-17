/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.persistence.api;

import java.util.List;

import com.hp.demo.plugable.persistence.common.model.NetworkDevice;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceSortKey;
import com.hp.demo.plugable.persistence.common.model.User;
import com.hp.demo.plugable.persistence.common.model.UserFilter;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.auth.Username;
import com.hp.util.model.persistence.Query;

/**
 * Query factory.
 * 
 * @param <C> type of the context provided to queries to enable execution
 * @author Fabiel Zuniga
 */
public interface QueryFactory<C> {

    /**
     * Gets the configuration query factory.
     * 
     * @return the network device query factory
     */
    public ConfigurationFactory<C> configuration();

    /**
     * Gets the network device query factory.
     * 
     * @return the network device query factory
     */
    public NetworkDeviceFactory<C> networkDevice();

    /**
     * Gets the user query factory.
     * 
     * @return the user query factory
     */
    public UserFactory<C> user();

    /**
     * Configuration factory.
     * 
     * @param <C> type of the context provided to queries to enable execution
     */
    public static interface ConfigurationFactory<C> {

        /**
         * Creates a query to create the database schema.
         * 
         * @return a query
         */
        public Query<Void, C> createSchema();
    }

    /**
     * Network device factory.
     * 
     * @param <C> type of the context provided to queries to enable execution
     */
    public static interface NetworkDeviceFactory<C> {

        /**
         * Creates a query to store a network device.
         * 
         * @param device device to store
         * @return a query
         */
        public Query<Void, C> store(NetworkDevice device);

        /**
         * Creates a query to load a network device.
         * 
         * @param id device's id
         * @return a query
         */
        public Query<NetworkDevice, C> get(Id<NetworkDevice, SerialNumber> id);

        /**
         * Creates a query to find network devices.
         * 
         * @param filter filter
         * @param sortSpecification sort specification
         * @return a query
         */
        public Query<List<NetworkDevice>, C> find(NetworkDeviceFilter filter,
                SortSpecification<NetworkDeviceSortKey> sortSpecification);

        /**
         * Creates a query to delete a network device.
         * 
         * @param id device's id
         * @return a query
         */
        public Query<Void, C> delete(Id<NetworkDevice, SerialNumber> id);
    }

    /**
     * User factory.
     * 
     * @param <C> type of the context provided to queries to enable execution
     */
    public static interface UserFactory<C> {

        /**
         * Creates a query to store a user.
         * 
         * @param user user to store
         * @return a query
         */
        public Query<Void, C> store(User user);

        /**
         * Creates a query to load a user.
         * 
         * @param id user's id
         * @return a query
         */
        public Query<User, C> get(Id<User, Username> id);

        /**
         * Creates a query to find users.
         * 
         * @param filter filter
         * @return a query
         */
        public Query<List<User>, C> find(UserFilter filter);

        /**
         * Creates a query to delete a user.
         * 
         * @param id user's id
         * @return a query
         */
        public Query<Void, C> delete(Id<User, Username> id);
    }
}
