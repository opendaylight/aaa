/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model;

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
import com.hp.util.model.persistence.PersistenceException;

/**
 * Persistence Service.
 * 
 * @author Fabiel Zuniga
 */
public interface PersistenceService {

    /**
     * Gets the network device persistence service.
     * 
     * @return the network device persistence service
     */
    public NetworkDevicePersistenceService networkDevice();

    /**
     * Gets the user persistence service.
     * 
     * @return the user persistence service
     */
    public UserPersistenceService user();

    /**
     * Network device persistence service.
     */
    public static interface NetworkDevicePersistenceService {

        /**
         * Stores a network device.
         * 
         * @param device device to store
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public void store(NetworkDevice device) throws PersistenceException;

        /**
         * Loads a network device.
         * 
         * @param id device's id
         * @return the device if found, {@code null} otherwise
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public NetworkDevice get(Id<NetworkDevice, SerialNumber> id) throws PersistenceException;

        /**
         * Finds network devices.
         * 
         * @param filter filter
         * @param sortSpecification sort specification
         * @return found devices
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public List<NetworkDevice> find(NetworkDeviceFilter filter,
                SortSpecification<NetworkDeviceSortKey> sortSpecification) throws PersistenceException;

        /**
         * Deletes a network device.
         * 
         * @param id device's id
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public void delete(Id<NetworkDevice, SerialNumber> id) throws PersistenceException;
    }

    /**
     * User persistence service.
     */
    public static interface UserPersistenceService {
        
        /**
         * Stores a user.
         * 
         * @param user user to store
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public void store(User user) throws PersistenceException;

        /**
         * Loads a user.
         * 
         * @param id user's id
         * @return the user if found, {@code null} otherwise
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public User get(Id<User, Username> id) throws PersistenceException;

        /**
         * Find users.
         * 
         * @param filter filter
         * @return found users
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public List<User> find(UserFilter filter) throws PersistenceException;

        /**
         * Deletes a user.
         * 
         * @param id user's id
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public void delete(Id<User, Username> id) throws PersistenceException;
    }
}
