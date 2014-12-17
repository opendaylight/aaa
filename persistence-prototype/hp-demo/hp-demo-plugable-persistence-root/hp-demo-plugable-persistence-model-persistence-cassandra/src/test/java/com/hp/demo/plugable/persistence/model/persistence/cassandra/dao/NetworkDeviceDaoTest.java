/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.persistence.cassandra.dao;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Ignore;

import com.hp.demo.plugable.persistence.common.model.Location;
import com.hp.demo.plugable.persistence.common.model.NetworkDevice;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceSortKey;
import com.hp.demo.plugable.persistence.model.persistence.cassandra.dao.NetworkDeviceDaoTest.AstyanaxNetworkDeviceDao;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.common.type.SortOrder;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.net.ReachabilityStatus;
import com.hp.util.model.persistence.cassandra.client.astyanax.Astyanax;
import com.hp.util.model.persistence.cassandra.dao.CassandraMarkPageDaoTest;
import com.hp.util.model.persistence.dao.SearchCase;
import com.hp.util.test.common.RandomDataGeneratorExtended;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
// TODO:
@Ignore
public class NetworkDeviceDaoTest
        extends
        CassandraMarkPageDaoTest<SerialNumber, NetworkDevice, NetworkDeviceFilter, NetworkDeviceSortKey, AstyanaxNetworkDeviceDao> {

    @Override
    protected AstyanaxNetworkDeviceDao createDaoInstance() {
        return new AstyanaxNetworkDeviceDao();
    }

    @Override
    protected boolean isVersioned() {
        return false;
    }

    @Override
    protected NetworkDevice createIdentifiable(Id<NetworkDevice, SerialNumber> id) {
        RandomDataGeneratorExtended randomDataGenerator = new RandomDataGeneratorExtended();
        NetworkDevice device = new NetworkDevice(id, randomDataGenerator.getMacAddress(),
                randomDataGenerator.getEnum(ReachabilityStatus.class));
        device.setFriendlyName("friendly name" + randomDataGenerator.getInt());
        device.setIpAddress(randomDataGenerator.getIpAddress());
        device.setLocation(randomDataGenerator.getEnum(Location.class));
        return device;
    }

    @Override
    protected void modify(NetworkDevice device) {
        RandomDataGeneratorExtended randomDataGenerator = new RandomDataGeneratorExtended();
        device.setFriendlyName("friendly name" + randomDataGenerator.getInt());
        device.setIpAddress(randomDataGenerator.getIpAddress());
        device.setLocation(randomDataGenerator.getEnum(Location.class));
        device.setReachabilityStatus(randomDataGenerator.getEnum(ReachabilityStatus.class));
    }

    @Override
    protected List<NetworkDevice> createIdentifiables(int count) {
        List<NetworkDevice> devices = new ArrayList<NetworkDevice>();
        for (int i = 0; i < count; i++) {
            Id<NetworkDevice, SerialNumber> id = Id.valueOf(SerialNumber.valueOf(String.valueOf(i)));
            devices.add(createIdentifiable(id));
        }
        return devices;
    }

    @Override
    protected void assertEqualState(NetworkDevice expected, NetworkDevice actual) {
        Assert.assertEquals(expected.getMacAddress(), actual.getMacAddress());
        Assert.assertEquals(expected.getIpAddress(), actual.getIpAddress());
        Assert.assertEquals(expected.getFriendlyName(), actual.getFriendlyName());
        Assert.assertEquals(expected.getLocation(), actual.getLocation());
        Assert.assertEquals(expected.getReachabilityStatus(), actual.getReachabilityStatus());
    }

    @Override
    protected List<SearchCase<NetworkDevice, NetworkDeviceFilter, NetworkDeviceSortKey>> getSearchCases() {
        List<SearchCase<NetworkDevice, NetworkDeviceFilter, NetworkDeviceSortKey>> searchCases = new ArrayList<SearchCase<NetworkDevice, NetworkDeviceFilter, NetworkDeviceSortKey>>();

        RandomDataGeneratorExtended randomDataGenerator = new RandomDataGeneratorExtended();

        Id<NetworkDevice, SerialNumber> id1 = Id.valueOf(SerialNumber.valueOf("1"));
        Id<NetworkDevice, SerialNumber> id2 = Id.valueOf(SerialNumber.valueOf("2"));
        Id<NetworkDevice, SerialNumber> id3 = Id.valueOf(SerialNumber.valueOf("3"));
        Id<NetworkDevice, SerialNumber> id4 = Id.valueOf(SerialNumber.valueOf("4"));
        Id<NetworkDevice, SerialNumber> id5 = Id.valueOf(SerialNumber.valueOf("5"));

        NetworkDevice dto1 = new NetworkDevice(id1, randomDataGenerator.getMacAddress(), ReachabilityStatus.REACHABLE);
        NetworkDevice dto2 = new NetworkDevice(id2, randomDataGenerator.getMacAddress(), ReachabilityStatus.REACHABLE);
        NetworkDevice dto3 = new NetworkDevice(id3, randomDataGenerator.getMacAddress(), ReachabilityStatus.UNREACHABLE);
        NetworkDevice dto4 = new NetworkDevice(id4, randomDataGenerator.getMacAddress(), ReachabilityStatus.REACHABLE);
        NetworkDevice dto5 = new NetworkDevice(id5, randomDataGenerator.getMacAddress(), ReachabilityStatus.UNREACHABLE);

        dto1.setLocation(Location.BUILDING_1_FIRST_FLOOR);
        dto2.setLocation(Location.BUILDING_1_SECOND_FLOOR);
        dto3.setLocation(Location.BUILDING_1_FIRST_FLOOR);
        dto4.setLocation(Location.BUILDING_1_SECOND_FLOOR);
        dto5.setLocation(Location.BUILDING_2_FIRST_FLOOR);

        dto1.setFriendlyName("device 1");
        dto2.setFriendlyName("device two");
        dto3.setFriendlyName("device 3");
        dto4.setFriendlyName("device four");
        dto5.setFriendlyName("device 5");

        List<NetworkDevice> searchSpace = new ArrayList<NetworkDevice>(5);
        searchSpace.add(dto1);
        searchSpace.add(dto2);
        searchSpace.add(dto3);
        searchSpace.add(dto4);
        searchSpace.add(dto5);

        // SQL systems normally use primary key as the default sorting

        //

        NetworkDeviceFilter filter = null;
        SortSpecification<NetworkDeviceSortKey> sortSpecification = null;
        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, searchSpace));

        //

        filter = NetworkDeviceFilter.filterAll();
        sortSpecification = new SortSpecification<NetworkDeviceSortKey>();
        sortSpecification.addSortComponent(NetworkDeviceSortKey.FRIENDLY_NAME, SortOrder.ASCENDING);
        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto1, dto3, dto5, dto4, dto2));

        //

        filter = NetworkDeviceFilter.filterByLocation(Location.BUILDING_1_SECOND_FLOOR);
        sortSpecification = new SortSpecification<NetworkDeviceSortKey>();
        sortSpecification.addSortComponent(NetworkDeviceSortKey.FRIENDLY_NAME, SortOrder.DESCENDING);
        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto2, dto4));

        //

        filter = NetworkDeviceFilter.filterByReachabilityStatus(ReachabilityStatus.REACHABLE);
        sortSpecification = new SortSpecification<NetworkDeviceSortKey>();
        sortSpecification.addSortComponent(NetworkDeviceSortKey.FRIENDLY_NAME, SortOrder.ASCENDING);
        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto1, dto4, dto2));

        //

        filter = NetworkDeviceFilter.filterByLocationAndByReachabilityStatus(Location.BUILDING_1_FIRST_FLOOR,
                ReachabilityStatus.UNREACHABLE);
        sortSpecification = new SortSpecification<NetworkDeviceSortKey>();
        sortSpecification.addSortComponent(NetworkDeviceSortKey.FRIENDLY_NAME, SortOrder.DESCENDING);
        searchCases.add(SearchCase.forCase(searchSpace, filter, sortSpecification, dto3));

        //

        return searchCases;
    }

    public static class AstyanaxNetworkDeviceDao extends NetworkDeviceDao<Astyanax> {
        /*
         * Class to allow using Astyanax-based integration test.
         */
    }
}
