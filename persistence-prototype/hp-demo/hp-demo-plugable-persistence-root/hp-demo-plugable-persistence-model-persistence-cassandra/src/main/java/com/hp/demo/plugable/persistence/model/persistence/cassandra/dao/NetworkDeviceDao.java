/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model.persistence.cassandra.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hp.demo.plugable.persistence.common.model.Location;
import com.hp.demo.plugable.persistence.common.model.NetworkDevice;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter.All;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter.ByLocation;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter.ByLocationAndReachabilityStatus;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceFilter.ByReachabilityStatus;
import com.hp.demo.plugable.persistence.common.model.NetworkDeviceSortKey;
import com.hp.util.common.BidirectionalConverter;
import com.hp.util.common.Converter;
import com.hp.util.common.Parser;
import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.converter.SerialNumberStringConverter;
import com.hp.util.common.parser.EnumParser;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.Property;
import com.hp.util.common.type.SerialNumber;
import com.hp.util.common.type.SerializableValueType;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.net.IpAddress;
import com.hp.util.common.type.net.MacAddress;
import com.hp.util.common.type.net.ReachabilityStatus;
import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraRow;
import com.hp.util.model.persistence.cassandra.MainColumnFamily;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.DynamicColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.EnumColumn;
import com.hp.util.model.persistence.cassandra.column.StringColumn;
import com.hp.util.model.persistence.cassandra.dao.CassandraMarkPageDao;
import com.hp.util.model.persistence.cassandra.index.AllRowsSecondaryIndex;
import com.hp.util.model.persistence.cassandra.index.CustomSecondaryIndex;
import com.hp.util.model.persistence.cassandra.index.EnumSecondaryIndex;
import com.hp.util.model.persistence.cassandra.index.IndexEntryHandler;
import com.hp.util.model.persistence.cassandra.index.IndexEntryHandler.IndexEntry;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeTypeSerializer;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;

/**
 * Network Device DAO.
 * <p>
 * This DAO does not use any of the native features of the Cassandra Client, so it can be
 * parameterized with any Native Cassandra Client.
 * 
 * @param <N> type of the native Cassandra client
 * @author Fabiel Zuniga
 */
public class NetworkDeviceDao<N> extends
        CassandraMarkPageDao<SerialNumber, String, NetworkDevice, NetworkDeviceFilter, NetworkDeviceSortKey, N> {

    private static final ColumnName<String, String> MAC_ADDRESS = ColumnName.valueOf("mac_address");
    private static final ColumnName<String, String> IP_ADDRESS = ColumnName.valueOf("ip_address");
    private static final ColumnName<String, Location> LOCATION = ColumnName.valueOf("location");
    private static final ColumnName<String, String> FRIENDLY_NAME = ColumnName.valueOf("friendly_name");
    private static final ColumnName<String, ReachabilityStatus> REACHABILITY_STATUS = ColumnName
            .valueOf("reachability_status");

    private static final DataType<Location> LOCATION_TYPE = EnumType.valueOf(Location.class);
    private static final DataType<ReachabilityStatus> REACHABILITY_STATUS_TYPE = EnumType
            .valueOf(ReachabilityStatus.class);

    private static final Parser<Location> LOCATION_PARSER = new EnumParser<Location>(Location.class);
    private static final Parser<ReachabilityStatus> REACHABILITY_STATUS_PARSER = new EnumParser<ReachabilityStatus>(
            ReachabilityStatus.class);

    private static final CompositeTypeSerializer<DenormalizedData> DENORMALIZED_DATA_SERIALIZER = new DenormalizedDataSerializer();
    private static final CompositeType<DenormalizedData> DENORMALIZED_DATA_TYPE = new CompositeType<DenormalizedData>(
            DENORMALIZED_DATA_SERIALIZER, BasicType.STRING_UTF8, BasicType.STRING_UTF8, BasicType.STRING_UTF8,
            BasicType.STRING_UTF8, BasicType.STRING_UTF8);
    private static final Converter<Column<String, DenormalizedData>, NetworkDevice> DENORMALIZED_DATA_CONVERTER = new DenormalizedDataConverter();

    private static final BidirectionalConverter<NetworkDevice, CassandraRow<SerialNumber, String>> CONVERTER = new NetworkDeviceConverter();

    private IndexEntryHandler<String> indexEntryHandler;
    private AllRowsSecondaryIndex<String, DenormalizedData> allRowsIndex;
    private CustomSecondaryIndex<Location, String, DenormalizedData> locationIndex;
    private CustomSecondaryIndex<ReachabilityStatus, String, DenormalizedData> reachabilityStatusIndex;

    /**
     * Creates a DAO.
     */
    public NetworkDeviceDao() {
        super(new MainColumnFamily<SerialNumber, String, NetworkDevice>("cf_network_device_main",
                BasicType.STRING_UTF8, "Network devices main column family", createColumnValueTypeProvider(),
                SerialNumberStringConverter.getInstance(), CONVERTER));

        this.indexEntryHandler = new IndexEntryHandler<String>("cf_user_index_entry_handler", BasicType.STRING_UTF8);
        this.allRowsIndex = new AllRowsSecondaryIndex<String, DenormalizedData>("cf_network_device_all_rows_index",
                BasicType.STRING_UTF8, DENORMALIZED_DATA_TYPE);
        this.locationIndex = new EnumSecondaryIndex<Location, String, DenormalizedData>(Location.class,
                "cf__network_device_location_index", BasicType.STRING_UTF8, DENORMALIZED_DATA_TYPE);
        this.reachabilityStatusIndex = new EnumSecondaryIndex<ReachabilityStatus, String, DenormalizedData>(
                ReachabilityStatus.class, "cf__network_device_reachability_status_index", BasicType.STRING_UTF8,
                DENORMALIZED_DATA_TYPE);
    }

    private static ColumnValueTypeProvider<String> createColumnValueTypeProvider() {
        DynamicColumnValueTypeProvider<String> columnValueTypeProvider = new DynamicColumnValueTypeProvider<String>();
        columnValueTypeProvider.registerColumnValueType(MAC_ADDRESS, BasicType.STRING_UTF8);
        columnValueTypeProvider.registerColumnValueType(IP_ADDRESS, BasicType.STRING_UTF8);
        columnValueTypeProvider.registerColumnValueType(LOCATION, LOCATION_TYPE);
        columnValueTypeProvider.registerColumnValueType(FRIENDLY_NAME, BasicType.STRING_UTF8);
        columnValueTypeProvider.registerColumnValueType(REACHABILITY_STATUS, REACHABILITY_STATUS_TYPE);
        return columnValueTypeProvider;
    }

    @Override
    protected Collection<ColumnFamily<?, ?>> getIndexesColumnFamilyDefinitions() {
        Collection<ColumnFamily<?, ?>> columnFamilyDefinitions = new ArrayList<ColumnFamily<?, ?>>();
        columnFamilyDefinitions.addAll(this.indexEntryHandler.getColumnFamilies());
        columnFamilyDefinitions.addAll(this.allRowsIndex.getColumnFamilies());
        columnFamilyDefinitions.addAll(this.locationIndex.getColumnFamilies());
        columnFamilyDefinitions.addAll(this.reachabilityStatusIndex.getColumnFamilies());
        return columnFamilyDefinitions;
    }

    private void clearIndexes(Id<NetworkDevice, SerialNumber> id, CassandraContext<N> context)
            throws PersistenceException {
        SerialNumber serialNumber = id.getValue();
        this.allRowsIndex.delete(serialNumber.getValue(), context);

        Collection<Column<IndexEntry, Location>> locationIndexedValues = this.indexEntryHandler.getIndexedValues(
                serialNumber.getValue(), LOCATION, LOCATION_TYPE, context);
        for (Column<IndexEntry, Location> indexValue : locationIndexedValues) {
            this.locationIndex.delete(serialNumber.getValue(), indexValue.getValue(), context);
        }
        this.indexEntryHandler.deleteIndexedValues(serialNumber.getValue(), locationIndexedValues, context);

        Collection<Column<IndexEntry, ReachabilityStatus>> reachabilityStatusIndexedValues = this.indexEntryHandler
                .getIndexedValues(serialNumber.getValue(), REACHABILITY_STATUS, REACHABILITY_STATUS_TYPE, context);
        for (Column<IndexEntry, ReachabilityStatus> indexValue : reachabilityStatusIndexedValues) {
            this.reachabilityStatusIndex.delete(serialNumber.getValue(), indexValue.getValue(), context);
        }
        this.indexEntryHandler.deleteIndexedValues(serialNumber.getValue(), reachabilityStatusIndexedValues, context);
    }

    @Override
    protected void updateIndexesBeforeInsertion(NetworkDevice device, CassandraContext<N> context)
            throws PersistenceException {
        clearIndexes(device.getId(), context);

        SerialNumber serialNumber = device.getId().getValue();
        DenormalizedData denormalizedData = DenormalizedDataConverter.toDenormalizedData(device);

        Column<String, Location> locationIndexEntry = new EnumColumn<String, Location>(LOCATION, device.getLocation());
        Column<String, ReachabilityStatus> reachabilityStatusIndexEntry = new EnumColumn<String, ReachabilityStatus>(
                REACHABILITY_STATUS, device.getReachabilityStatus());

        if (locationIndexEntry.getValue() != null) {
            this.indexEntryHandler.addIndexedValue(serialNumber.getValue(), locationIndexEntry, context);
        }
        this.indexEntryHandler.addIndexedValue(serialNumber.getValue(), reachabilityStatusIndexEntry, context);

        this.allRowsIndex.insert(serialNumber.getValue(), denormalizedData, context);
        if (locationIndexEntry.getValue() != null) {
            this.locationIndex
                    .insert(serialNumber.getValue(), denormalizedData, locationIndexEntry.getValue(), context);
        }
        this.reachabilityStatusIndex.insert(serialNumber.getValue(), denormalizedData,
                reachabilityStatusIndexEntry.getValue(), context);
    }

    @Override
    protected void updateIndexesBeforeDeletion(Id<NetworkDevice, SerialNumber> id, CassandraContext<N> context)
            throws PersistenceException {
        clearIndexes(id, context);
    }

    @Override
    public List<NetworkDevice> find(NetworkDeviceFilter networkDeviceFilter,
            final SortSpecification<NetworkDeviceSortKey> sortSpecification, final CassandraContext<N> context)
            throws PersistenceException {
        NetworkDeviceFilter.Visitor<List<Column<String, DenormalizedData>>> visitor = new NetworkDeviceFilter.Visitor<List<Column<String, DenormalizedData>>>() {

            @Override
            public List<Column<String, DenormalizedData>> visit(All filter) {
                try {
                    return NetworkDeviceDao.this.allRowsIndex.read(context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public List<Column<String, DenormalizedData>> visit(ByLocation filter) {
                try {
                    // UNEQUAL is not exposed by the filter.
                    Location location = filter.getLocationCondition().getValue();
                    return NetworkDeviceDao.this.locationIndex.read(location, context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public List<Column<String, DenormalizedData>> visit(ByReachabilityStatus filter) {
                try {
                    // UNEQUAL is not exposed by the filter.
                    ReachabilityStatus reachabilityStatus = filter.getReachabilityStatusCondition().getValue();
                    return NetworkDeviceDao.this.reachabilityStatusIndex.read(reachabilityStatus, context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public List<Column<String, DenormalizedData>> visit(ByLocationAndReachabilityStatus filter) {
                // TODO:
                throw new RuntimeException("Missing implementation...");
            }
        };

        List<Column<String, DenormalizedData>> indexedColumns = Collections.emptyList();
        try {
            indexedColumns = nonnull(networkDeviceFilter).accept(visitor);
        }
        catch (RuntimeException e) {
            rethrow(e);
        }

        List<NetworkDevice> result = new ArrayList<NetworkDevice>(indexedColumns.size());
        for (Column<String, DenormalizedData> indexedColumn : indexedColumns) {
            result.add(DENORMALIZED_DATA_CONVERTER.convert(indexedColumn));
        }

        return result;
    }

    @Override
    public MarkPage<NetworkDevice> find(NetworkDeviceFilter networkDeviceFilter,
            final SortSpecification<NetworkDeviceSortKey> sortSpecification,
            MarkPageRequest<NetworkDevice> pageRequest, final CassandraContext<N> context) throws PersistenceException {
        if (pageRequest == null) {
            throw new NullPointerException("pageRequest cannot be null");
        }

        Id<NetworkDevice, SerialNumber> markId = pageRequest.getMark() != null ? pageRequest.getMark().getId() : null;
        SerialNumber markIndexValue = SerializableValueType.toValue(markId);
        final MarkPageRequest<String> indexPageRequest = pageRequest.convert(markIndexValue != null ? markIndexValue
                .getValue() : null);

        NetworkDeviceFilter.Visitor<MarkPage<Column<String, DenormalizedData>>> visitor = new NetworkDeviceFilter.Visitor<MarkPage<Column<String, DenormalizedData>>>() {

            @Override
            public MarkPage<Column<String, DenormalizedData>> visit(All filter) {
                try {
                    return NetworkDeviceDao.this.allRowsIndex.read(indexPageRequest, context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public MarkPage<Column<String, DenormalizedData>> visit(ByLocation filter) {
                try {
                    // UNEQUAL is not exposed by the filter.
                    Location location = filter.getLocationCondition().getValue();
                    return NetworkDeviceDao.this.locationIndex.read(location, indexPageRequest, context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public MarkPage<Column<String, DenormalizedData>> visit(ByReachabilityStatus filter) {
                try {
                    // UNEQUAL is not exposed by the filter.
                    ReachabilityStatus reachabilityStatus = filter.getReachabilityStatusCondition().getValue();
                    return NetworkDeviceDao.this.reachabilityStatusIndex.read(reachabilityStatus, indexPageRequest,
                            context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public MarkPage<Column<String, DenormalizedData>> visit(ByLocationAndReachabilityStatus filter) {
                throw new RuntimeException("Missing implementation...");
            }
        };

        MarkPage<Column<String, DenormalizedData>> page = MarkPage.emptyPage();
        try {
            page = nonnull(networkDeviceFilter).accept(visitor);
        }
        catch (RuntimeException e) {
            rethrow(e);
        }

        return page.convert(DENORMALIZED_DATA_CONVERTER);
    }

    @Override
    public long count(NetworkDeviceFilter networkDeviceFilter, final CassandraContext<N> context)
            throws PersistenceException {
        NetworkDeviceFilter.Visitor<Long> visitor = new NetworkDeviceFilter.Visitor<Long>() {

            @Override
            public Long visit(All filter) {
                try {
                    return Long.valueOf(NetworkDeviceDao.this.allRowsIndex.count(context));
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Long visit(ByLocation filter) {
                try {
                    // UNEQUAL is not exposed by the filter.
                    Location location = filter.getLocationCondition().getValue();
                    return Long.valueOf(NetworkDeviceDao.this.locationIndex.count(location, context));
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Long visit(ByReachabilityStatus filter) {
                try {
                    // UNEQUAL is not exposed by the filter.
                    ReachabilityStatus reachabilityStatus = filter.getReachabilityStatusCondition().getValue();
                    return Long.valueOf(NetworkDeviceDao.this.reachabilityStatusIndex
                            .count(reachabilityStatus, context));
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Long visit(ByLocationAndReachabilityStatus filter) {
                // TODO:
                throw new RuntimeException("Missing implementation...");
            }
        };

        long count = 0;
        try {
            count = nonnull(networkDeviceFilter).accept(visitor).longValue();
        }
        catch (RuntimeException e) {
            rethrow(e);
        }
        return count;
    }

    private static NetworkDeviceFilter nonnull(NetworkDeviceFilter filter) {
        if (filter != null) {
            return filter;
        }
        return NetworkDeviceFilter.filterAll();
    }

    private static class NetworkDeviceConverter implements
            BidirectionalConverter<NetworkDevice, CassandraRow<SerialNumber, String>> {

        @Override
        public CassandraRow<SerialNumber, String> convert(NetworkDevice source) {
            CassandraRow<SerialNumber, String> row = new CassandraRow<SerialNumber, String>(source.getId().getValue());
            row.setColumn(new StringColumn<String>(MAC_ADDRESS, SerializableValueType.toValue(source.getMacAddress())));
            row.setColumn(new StringColumn<String>(IP_ADDRESS, SerializableValueType.toValue(source.getIpAddress())));
            row.setColumn(new EnumColumn<String, Location>(LOCATION, source.getLocation()));
            row.setColumn(new StringColumn<String>(FRIENDLY_NAME, source.getFriendlyName()));
            row.setColumn(new EnumColumn<String, ReachabilityStatus>(REACHABILITY_STATUS, source
                    .getReachabilityStatus()));
            return row;
        }

        @Override
        public NetworkDevice restore(CassandraRow<SerialNumber, String> target) throws IllegalArgumentException {
            String strMacAddress = (String) target.getColumn(MAC_ADDRESS).getValue();
            String strIpAddress = (String) target.getColumn(IP_ADDRESS).getValue();

            MacAddress macAddress = strMacAddress != null ? MacAddress.valueOf(strMacAddress) : null;
            IpAddress ipAddress = strIpAddress != null ? IpAddress.valueOf(strIpAddress) : null;
            Location location = (Location) target.getColumn(LOCATION).getValue();
            String friendlyName = (String) target.getColumn(FRIENDLY_NAME).getValue();
            ReachabilityStatus reachabilityStatus = (ReachabilityStatus) target.getColumn(REACHABILITY_STATUS)
                    .getValue();

            Id<NetworkDevice, SerialNumber> id = Id.valueOf(target.getKey());
            NetworkDevice device = new NetworkDevice(id, macAddress, reachabilityStatus);
            device.setIpAddress(ipAddress);
            device.setLocation(location);
            device.setFriendlyName(friendlyName);
            return device;
        }
    }

    private static class DenormalizedData {

        private String macAddress;
        private String ipAddress;
        private String location;
        private String friendlyName;
        private String reachabilityStatus;

        public DenormalizedData(String macAddress, String ipAddress, String location, String friendlyName,
                String reachabilityStatus) {
            this.macAddress = macAddress;
            this.ipAddress = ipAddress;
            this.location = location;
            this.friendlyName = friendlyName;
            this.reachabilityStatus = reachabilityStatus;
        }

        public String getMacAddress() {
            return this.macAddress;
        }

        public String getIpAddress() {
            return this.ipAddress;
        }

        public String getLocation() {
            return this.location;
        }

        public String getFriendlyName() {
            return this.friendlyName;
        }

        public String getReachabilityStatus() {
            return this.reachabilityStatus;
        }

        @Override
        public String toString() {
            return ObjectToStringConverter.toString(this, Property.valueOf("macAddress", this.macAddress),
                    Property.valueOf("ipAddress", this.ipAddress), Property.valueOf("location", this.location),
                    Property.valueOf("friendlyName", this.friendlyName),
                    Property.valueOf("reachabilityStatus", this.reachabilityStatus));
        }
    }

    private static class DenormalizedDataSerializer implements CompositeTypeSerializer<DenormalizedData> {

        @Override
        public List<Component<DenormalizedData, ?>> serialize(DenormalizedData compositeValue) {
            List<Component<DenormalizedData, ?>> components = new ArrayList<Component<DenormalizedData, ?>>();
            components.add(new Component<DenormalizedData, String>(BasicType.STRING_UTF8, compositeValue
                    .getMacAddress()));
            components
                    .add(new Component<DenormalizedData, String>(BasicType.STRING_UTF8, compositeValue.getIpAddress()));
            components
                    .add(new Component<DenormalizedData, String>(BasicType.STRING_UTF8, compositeValue.getLocation()));
            components.add(new Component<DenormalizedData, String>(BasicType.STRING_UTF8, compositeValue
                    .getFriendlyName()));
            components.add(new Component<DenormalizedData, String>(BasicType.STRING_UTF8, compositeValue
                    .getReachabilityStatus()));
            return components;
        }

        @Override
        public DenormalizedData deserialize(List<Component<DenormalizedData, ?>> components) {
            String macAddress = (String) components.get(0).getValue();
            String ipAddress = (String) components.get(1).getValue();
            String location = (String) components.get(2).getValue();
            String friendlyName = (String) components.get(3).getValue();
            String reachabilityStatus = (String) components.get(4).getValue();
            return new DenormalizedData(macAddress, ipAddress, location, friendlyName, reachabilityStatus);
        }
    }

    private static class DenormalizedDataConverter implements
            Converter<Column<String, DenormalizedData>, NetworkDevice> {

        public static DenormalizedData toDenormalizedData(NetworkDevice device) {
            return new DenormalizedData(SerializableValueType.toValue(device.getMacAddress()),
                    SerializableValueType.toValue(device.getIpAddress()), LOCATION_PARSER.toParsable(device
                            .getLocation()), device.getFriendlyName(), REACHABILITY_STATUS_PARSER.toParsable(device
                            .getReachabilityStatus()));
        }

        @Override
        public NetworkDevice convert(Column<String, DenormalizedData> indexedColumn) {
            SerialNumber serialNumber = SerialNumber.valueOf(indexedColumn.getName().getValue());
            DenormalizedData denormalizedData = indexedColumn.getValue();

            MacAddress macAddress = denormalizedData.getMacAddress() != null ? MacAddress.valueOf(denormalizedData
                    .getMacAddress()) : null;
            IpAddress ipAddress = denormalizedData.getIpAddress() != null ? IpAddress.valueOf(denormalizedData
                    .getIpAddress()) : null;
            Location location = LOCATION_PARSER.parse(denormalizedData.getLocation());
            String friendlyName = denormalizedData.getFriendlyName();
            ReachabilityStatus reachabilityStatus = REACHABILITY_STATUS_PARSER.parse(denormalizedData
                    .getReachabilityStatus());

            Id<NetworkDevice, SerialNumber> id = Id.valueOf(serialNumber);
            NetworkDevice device = new NetworkDevice(id, macAddress, reachabilityStatus);
            device.setIpAddress(ipAddress);
            device.setLocation(location);
            device.setFriendlyName(friendlyName);
            return device;
        }
    }
}
