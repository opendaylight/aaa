/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model;

import java.util.List;

import com.hp.demo.plugable.persistence.common.model.Location;
import com.hp.demo.plugable.persistence.common.model.NetworkDevice;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.common.type.net.IpAddress;

/**
 * Business logic for network devices.
 * 
 * @author Fabiel Zuniga
 */
public interface NetworkDeviceService {

    /**
     * Discovers a network device.
     * 
     * @param ipAddress IP Address of the device to discover
     * @return the discovered device
     */
    public NetworkDevice discover(IpAddress ipAddress);

    /**
     * Sets the device's friendly name.
     * 
     * @param id id of the device to update
     * @param friendlyName friendly name
     */
    public void setFriendlyName(Id<NetworkDevice, SerialNumber> id, String friendlyName);

    /**
     * Sets the device's friendly name.
     * 
     * @param id id of the device to update
     * @param location location
     */
    public void setLocation(Id<NetworkDevice, SerialNumber> id, Location location);

    /**
     * Gets the reachable devices.
     * 
     * @return reachable devices
     */
    public List<NetworkDevice> getReachable();

    /**
     * Gets the unreachable devices.
     * 
     * @return unreachable devices
     */
    public List<NetworkDevice> getUnreachable();

    /**
     * Gets devices by location.
     * 
     * @param location location
     * @return devices located at {@code location}
     */
    public List<NetworkDevice> getByLocation(Location location);
}
