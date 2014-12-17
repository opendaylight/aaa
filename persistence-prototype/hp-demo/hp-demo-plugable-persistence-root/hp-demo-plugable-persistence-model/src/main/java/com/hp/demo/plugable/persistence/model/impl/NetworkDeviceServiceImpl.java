/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.impl;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.hp.demo.plugable.persistence.common.model.Location;
import com.hp.demo.plugable.persistence.common.model.NetworkDevice;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter;
import com.hp.demo.plugable.persistence.model.NetworkDeviceService;
import com.hp.demo.plugable.persistence.model.PersistenceService;
import com.hp.util.common.log.Logger;
import com.hp.util.common.log.LoggerProvider;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.common.type.net.IpAddress;
import com.hp.util.common.type.net.MacAddress;
import com.hp.util.common.type.net.ReachabilityStatus;
import com.hp.util.model.persistence.PersistenceException;

/**
 * @author Fabiel Zuniga
 */
class NetworkDeviceServiceImpl implements NetworkDeviceService {

    private final PersistenceService persistenceService;
    private final AtomicLong idCount;
    private final Random random;
    private final Logger logger;

    public NetworkDeviceServiceImpl(PersistenceService persistenceService, LoggerProvider<Class<?>> loggerProvider) {
        this.persistenceService = persistenceService;
        this.idCount = new AtomicLong(1);
        this.random = new Random();
        this.logger = loggerProvider.getLogger(getClass());
    }

    @Override
    public NetworkDevice discover(IpAddress ipAddress) {
        SerialNumber serialNumber = SerialNumber.valueOf(String.valueOf(this.idCount.getAndIncrement()));
        Id<NetworkDevice, SerialNumber> id = Id.valueOf(serialNumber);
        MacAddress macAddress = getMacAddress();
        NetworkDevice device = new NetworkDevice(id, macAddress, ReachabilityStatus.REACHABLE);
        device.setIpAddress(ipAddress);
        try {
            this.persistenceService.networkDevice().store(device);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to discover device: " + ipAddress, e);
            throw new RuntimeException("Unable to discover device: " + ipAddress);
        }
        return device;
    }

    @Override
    public void setFriendlyName(Id<NetworkDevice, SerialNumber> id, String friendlyName) {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }

        NetworkDevice device = null;
        try {
            device = this.persistenceService.networkDevice().get(id);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to load device with id " + id, e);
            throw new RuntimeException("Unable to load device with id " + id);
        }

        device.setFriendlyName(friendlyName);

        try {
            this.persistenceService.networkDevice().store(device);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to store device with id " + id, e);
            throw new RuntimeException("Unable to store device with id " + id);
        }
    }

    @Override
    public void setLocation(Id<NetworkDevice, SerialNumber> id, Location location) {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }

        NetworkDevice device = null;
        try {
            device = this.persistenceService.networkDevice().get(id);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to load device with id " + id, e);
            throw new RuntimeException("Unable to load device with id " + id);
        }

        device.setLocation(location);

        try {
            this.persistenceService.networkDevice().store(device);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to store device with id " + id, e);
            throw new RuntimeException("Unable to store device with id " + id);
        }
    }

    @Override
    public List<NetworkDevice> getReachable() {
        return getByReachabilityStatus(ReachabilityStatus.REACHABLE);
    }

    @Override
    public List<NetworkDevice> getUnreachable() {
        return getByReachabilityStatus(ReachabilityStatus.UNREACHABLE);
    }

    private List<NetworkDevice> getByReachabilityStatus(ReachabilityStatus reachabilityStatus) {
        NetworkDeviceFilter filter = NetworkDeviceFilter.filterByReachabilityStatus(reachabilityStatus);
        try {
            return this.persistenceService.networkDevice().find(filter, null);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to retrieve devices", e);
            throw new RuntimeException("Unable to retrieve devices");
        }
    }

    @Override
    public List<NetworkDevice> getByLocation(Location location) {
        NetworkDeviceFilter filter = NetworkDeviceFilter.filterByLocation(location);
        try {
            return this.persistenceService.networkDevice().find(filter, null);
        }
        catch (PersistenceException e) {
            this.logger.error("Unable to retrieve devices", e);
            throw new RuntimeException("Unable to retrieve devices");
        }
    }

    private MacAddress getMacAddress() {
        int intValue = this.random.nextInt();
        byte[] intBytes = new byte[] { (byte) (intValue >>> 24), (byte) (intValue >>> 16), (byte) (intValue >>> 8),
                (byte) intValue };
        return MacAddress.valueOfOctets((byte) 0, (byte) 0, intBytes[0], intBytes[1], intBytes[2], intBytes[3]);
    }
}
