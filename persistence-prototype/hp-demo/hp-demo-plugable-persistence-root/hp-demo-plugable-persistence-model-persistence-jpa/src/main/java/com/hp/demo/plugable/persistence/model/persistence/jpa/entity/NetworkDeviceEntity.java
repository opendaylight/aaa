/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.persistence.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.hp.demo.plugable.persistence.common.model.Location;
import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.common.type.net.IpAddress;
import com.hp.util.common.type.net.MacAddress;
import com.hp.util.common.type.net.ReachabilityStatus;

/**
 * Network device entity.
 * <p>
 * This class is not thread safe.
 * 
 * @author Fabiel Zuniga
 */
@Entity
@Table(name = "network_device")
public class NetworkDeviceEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "mac_address", nullable = false)
    private String macAddress;

    @Column(name = "ip_address")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "location")
    private Location location;

    @Column(name = "friendly_name", length = 128)
    private String friendlyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "reachability_status")
    private ReachabilityStatus reachabilityStatus;

    /**
     * This constructor is provided because of a restriction imposed by JPA and should not be used.
     */
    @Deprecated
    public NetworkDeviceEntity() {

    }

    /**
     * Create a network device entity
     * 
     * @param id id
     * @param macAddress MAC address
     * @param reachabilityStatus reachability status
     * @throws NullPointerException if any of the parameters is {@code null}
     */
    public NetworkDeviceEntity(SerialNumber id, MacAddress macAddress, ReachabilityStatus reachabilityStatus) {
        if (id == null) {
            throw new NullPointerException("id cannot be null");
        }

        if (macAddress == null) {
            throw new NullPointerException("macAddress cannot be null");
        }

        if (reachabilityStatus == null) {
            throw new NullPointerException("reachabilityStatus cannot be null");
        }

        this.id = id.getValue();
        this.macAddress = macAddress.getValue();
        this.reachabilityStatus = reachabilityStatus;
    }

    /**
     * Returns the id
     * 
     * @return the id
     */
    public SerialNumber getId() {
        return SerialNumber.valueOf(this.id);
    }

    /**
     * @return the macAddress
     */
    public MacAddress getMacAddress() {
        return MacAddress.valueOf(this.macAddress);
    }

    /**
     * @return the ipAddress
     */
    public IpAddress getIpAddress() {
        IpAddress value = null;
        if (this.ipAddress != null) {
            value = IpAddress.valueOf(this.ipAddress);
        }
        return value;
    }

    /**
     * @param ipAddress the ipAddress to set
     */
    public void setIpAddress(IpAddress ipAddress) {
        this.ipAddress = null;
        if (ipAddress != null) {
            this.ipAddress = ipAddress.getValue();
        }
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
