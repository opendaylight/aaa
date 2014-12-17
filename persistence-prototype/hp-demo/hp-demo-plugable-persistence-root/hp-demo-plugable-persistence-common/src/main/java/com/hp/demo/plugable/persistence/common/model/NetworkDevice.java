/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.common.model;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.model.SerializableAbstractIdentifiable;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.Property;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.common.type.net.IpAddress;
import com.hp.util.common.type.net.MacAddress;
import com.hp.util.common.type.net.ReachabilityStatus;

/**
 * Network Device.
 * 
 * @author Fabiel Zuniga
 */
public final class NetworkDevice extends SerializableAbstractIdentifiable<NetworkDevice, SerialNumber> {
    private static final long serialVersionUID = 1L;

    private final MacAddress macAddress;
    private IpAddress ipAddress;
    private Location location;
    private String friendlyName;
    private ReachabilityStatus reachabilityStatus;

    /**
     * Creates a network device.
     * 
     * @param id device's id
     * @param macAddress device's MAC address
     * @param reachabilityStatus reachability status
     */
    public NetworkDevice(Id<NetworkDevice, SerialNumber> id, MacAddress macAddress,
            ReachabilityStatus reachabilityStatus) {
        super(id);

        /*
         * In reality MAC Address may change, for for illustration purposes it is assumed it never
         * changes.
         */
        if (macAddress == null) {
            throw new NullPointerException("macAddress cannot be null");
        }

        this.macAddress = macAddress;

        /*
         * setReachabilityStatus is final and thus it is no longer a foreign method. So it is safe
         * to be called in a constructor.
         */
        setReachabilityStatus(reachabilityStatus);
    }

    /**
     * @return the macAddress
     */
    public MacAddress getMacAddress() {
        return this.macAddress;
    }

    /**
     * @return the ipAddress
     */
    public IpAddress getIpAddress() {
        return this.ipAddress;
    }

    /**
     * @param ipAddress the ipAddress to set
     */
    public void setIpAddress(IpAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return the friendlyName
     */
    public String getFriendlyName() {
        return this.friendlyName;
    }

    /**
     * @param friendlyName the friendlyName to set
     */
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    /**
     * @return the reachabilityStatus
     */
    public ReachabilityStatus getReachabilityStatus() {
        return this.reachabilityStatus;
    }

    /**
     * @param reachabilityStatus the reachabilityStatus to set
     */
    public final void setReachabilityStatus(ReachabilityStatus reachabilityStatus) {
        if (reachabilityStatus == null) {
            throw new NullPointerException("reachabilityStatus cannot be null");
        }
        this.reachabilityStatus = reachabilityStatus;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("id", getId()),
                Property.valueOf("macAddress", this.macAddress),
                Property.valueOf("ipAddress", this.ipAddress),
                Property.valueOf("location", this.location),
                Property.valueOf("friendlyName", this.friendlyName),
                Property.valueOf("reachabilityStatus", this.reachabilityStatus)
        );
    }
}
