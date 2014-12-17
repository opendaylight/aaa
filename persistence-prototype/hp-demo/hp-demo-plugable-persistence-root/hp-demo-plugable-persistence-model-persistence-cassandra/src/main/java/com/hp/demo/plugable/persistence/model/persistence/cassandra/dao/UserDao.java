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

import com.hp.demo.plugable.persistence.common.model.User;
import com.hp.demo.plugable.persistence.common.model.UserFilter;
import com.hp.demo.plugable.persistence.common.model.UserFilter.All;
import com.hp.demo.plugable.persistence.common.model.UserFilter.ByEnabledStatus;
import com.hp.util.common.BidirectionalConverter;
import com.hp.util.common.Converter;
import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.converter.UsernameStringConverter;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.Property;
import com.hp.util.common.type.SerializableValueType;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.auth.Password;
import com.hp.util.common.type.auth.Username;
import com.hp.util.common.type.net.Email;
import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraRow;
import com.hp.util.model.persistence.cassandra.MainColumnFamily;
import com.hp.util.model.persistence.cassandra.column.BooleanColumn;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.DynamicColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.StringColumn;
import com.hp.util.model.persistence.cassandra.dao.CassandraMarkPageDao;
import com.hp.util.model.persistence.cassandra.index.AllRowsSecondaryIndex;
import com.hp.util.model.persistence.cassandra.index.CustomSecondaryIndex;
import com.hp.util.model.persistence.cassandra.index.GenericCustomSecondaryIndex;
import com.hp.util.model.persistence.cassandra.index.IndexEntryHandler;
import com.hp.util.model.persistence.cassandra.index.IndexEntryHandler.IndexEntry;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeType;
import com.hp.util.model.persistence.cassandra.keyspace.CompositeTypeSerializer;

/**
 * User DAO.
 * <p>
 * This DAO does not use any of the native features of the Cassandra Client, so it can be
 * parameterized with any Native Cassandra Client.
 * 
 * @param <N> type of the native Cassandra client
 * @author Fabiel Zuniga
 */
public class UserDao<N> extends CassandraMarkPageDao<Username, String, User, UserFilter, Void, N> {

    private static final ColumnName<String, String> PASSWORD = ColumnName.valueOf("password");
    private static final ColumnName<String, String> EMAIL = ColumnName.valueOf("email");
    private static final ColumnName<String, String> DESCRIPTION = ColumnName.valueOf("description");
    private static final ColumnName<String, Boolean> ENABLED = ColumnName.valueOf("enabled");

    private static final CompositeTypeSerializer<DenormalizedData> DENORMALIZED_DATA_SERIALIZER = new DenormalizedDataSerializer();
    private static final CompositeType<DenormalizedData> DENORMALIZED_DATA_TYPE = new CompositeType<DenormalizedData>(
            DENORMALIZED_DATA_SERIALIZER, BasicType.STRING_UTF8, BasicType.STRING_UTF8, BasicType.STRING_UTF8,
            BasicType.BOOLEAN);
    private static final Converter<Column<String, DenormalizedData>, User> DENORMALIZED_DATA_CONVERTER = new DenormalizedDataConverter();

    private static final BidirectionalConverter<User, CassandraRow<Username, String>> CONVERTER = new UserConverter();

    private IndexEntryHandler<String> indexEntryHandler;
    private AllRowsSecondaryIndex<String, DenormalizedData> allRowsIndex;
    private CustomSecondaryIndex<Boolean, String, DenormalizedData> enabledIndex;

    /**
     * Creates a DAO.
     */
    public UserDao() {
        super(new MainColumnFamily<Username, String, User>("cf_user_main", BasicType.STRING_UTF8,
                "Users main column family", createColumnValueTypeProvider(), UsernameStringConverter.getInstance(),
                CONVERTER));

        this.indexEntryHandler = new IndexEntryHandler<String>("cf_user_index_entry_handler", BasicType.STRING_UTF8);
        this.allRowsIndex = new AllRowsSecondaryIndex<String, DenormalizedData>("cf_user_all_rows_index",
                BasicType.STRING_UTF8, DENORMALIZED_DATA_TYPE);
        this.enabledIndex = new GenericCustomSecondaryIndex<Boolean, String, DenormalizedData>(
                new ColumnFamily<Boolean, String>("cf_user_enabling_status_index", BasicType.BOOLEAN,
                        BasicType.STRING_UTF8, "Secondary index by enabled status"), DENORMALIZED_DATA_TYPE);
    }

    private static ColumnValueTypeProvider<String> createColumnValueTypeProvider() {
        DynamicColumnValueTypeProvider<String> columnValueTypeProvider = new DynamicColumnValueTypeProvider<String>();
        columnValueTypeProvider.registerColumnValueType(PASSWORD, BasicType.STRING_UTF8);
        columnValueTypeProvider.registerColumnValueType(EMAIL, BasicType.STRING_UTF8);
        columnValueTypeProvider.registerColumnValueType(DESCRIPTION, BasicType.STRING_UTF8);
        columnValueTypeProvider.registerColumnValueType(ENABLED, BasicType.BOOLEAN);
        return columnValueTypeProvider;
    }

    @Override
    protected Collection<ColumnFamily<?, ?>> getIndexesColumnFamilyDefinitions() {
        Collection<ColumnFamily<?, ?>> columnFamilyDefinitions = new ArrayList<ColumnFamily<?, ?>>();
        columnFamilyDefinitions.addAll(this.indexEntryHandler.getColumnFamilies());
        columnFamilyDefinitions.addAll(this.allRowsIndex.getColumnFamilies());
        columnFamilyDefinitions.addAll(this.enabledIndex.getColumnFamilies());
        return columnFamilyDefinitions;
    }

    private void clearIndexes(Id<User, Username> id, CassandraContext<N> context) throws PersistenceException {
        Username username = id.getValue();
        this.allRowsIndex.delete(username.getValue(), context);

        Collection<Column<IndexEntry, Boolean>> enabledIndexedValues = this.indexEntryHandler.getIndexedValues(
                username.getValue(), ENABLED, BasicType.BOOLEAN, context);
        for (Column<IndexEntry, Boolean> indexValue : enabledIndexedValues) {
            this.enabledIndex.delete(username.getValue(), indexValue.getValue(), context);
        }
        this.indexEntryHandler.deleteIndexedValues(username.getValue(), enabledIndexedValues, context);
    }

    @Override
    protected void updateIndexesBeforeInsertion(User user, CassandraContext<N> context) throws PersistenceException {
        clearIndexes(user.getId(), context);

        Username username = user.getId().getValue();
        DenormalizedData denormalizedData = DenormalizedDataConverter.toDenormalizedData(user);

        Column<String, Boolean> enabledIndexEntry = new BooleanColumn<String>(ENABLED,
                Boolean.valueOf(user.isEnabled()));
        this.indexEntryHandler.addIndexedValue(username.getValue(), enabledIndexEntry, context);

        this.allRowsIndex.insert(username.getValue(), denormalizedData, context);
        this.enabledIndex.insert(username.getValue(), denormalizedData, enabledIndexEntry.getValue(), context);
    }

    @Override
    protected void updateIndexesBeforeDeletion(Id<User, Username> id, CassandraContext<N> context)
            throws PersistenceException {
        clearIndexes(id, context);
    }

    @Override
    public List<User> find(UserFilter userFilter, final SortSpecification<Void> sortSpecification,
            final CassandraContext<N> context) throws PersistenceException {
        UserFilter.Visitor<List<Column<String, DenormalizedData>>> visitor = new UserFilter.Visitor<List<Column<String, DenormalizedData>>>() {

            @Override
            public List<Column<String, DenormalizedData>> visit(All filter) {
                try {
                    return UserDao.this.allRowsIndex.read(context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public List<Column<String, DenormalizedData>> visit(ByEnabledStatus filter) {
                try {
                    // UNEQUAL is not exposed by the filter.
                    Boolean indexKey = filter.getEnabledStatusCondition().getValue();
                    return UserDao.this.enabledIndex.read(indexKey, context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        List<Column<String, DenormalizedData>> indexedColumns = Collections.emptyList();
        try {
            indexedColumns = nonnull(userFilter).accept(visitor);
        }
        catch (RuntimeException e) {
            rethrow(e);
        }

        List<User> result = new ArrayList<User>(indexedColumns.size());
        for (Column<String, DenormalizedData> indexedColumn : indexedColumns) {
            result.add(DENORMALIZED_DATA_CONVERTER.convert(indexedColumn));
        }

        return result;
    }

    @Override
    public MarkPage<User> find(UserFilter userFilter, final SortSpecification<Void> sortSpecification,
            MarkPageRequest<User> pageRequest, final CassandraContext<N> context) throws PersistenceException {
        if (pageRequest == null) {
            throw new NullPointerException("pageRequest cannot be null");
        }

        Id<User, Username> markId = pageRequest.getMark() != null ? pageRequest.getMark().getId() : null;
        Username markIndexValue = SerializableValueType.toValue(markId);
        final MarkPageRequest<String> indexPageRequest = pageRequest.convert(markIndexValue != null ? markIndexValue
                .getValue() : null);

        UserFilter.Visitor<MarkPage<Column<String, DenormalizedData>>> visitor = new UserFilter.Visitor<MarkPage<Column<String, DenormalizedData>>>() {

            @Override
            public MarkPage<Column<String, DenormalizedData>> visit(All filter) {
                try {
                    return UserDao.this.allRowsIndex.read(indexPageRequest, context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public MarkPage<Column<String, DenormalizedData>> visit(ByEnabledStatus filter) {
                try {
                    Boolean indexKey = filter.getEnabledStatusCondition().getValue();
                    // UNEQUAL is not exposed by the filter.
                    return UserDao.this.enabledIndex.read(indexKey, indexPageRequest, context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        MarkPage<Column<String, DenormalizedData>> page = MarkPage.emptyPage();
        try {
            page = nonnull(userFilter).accept(visitor);
        }
        catch (RuntimeException e) {
            rethrow(e);
        }

        return page.convert(DENORMALIZED_DATA_CONVERTER);
    }

    @Override
    public long count(UserFilter userFilter, final CassandraContext<N> context) throws PersistenceException {
        UserFilter.Visitor<Long> visitor = new UserFilter.Visitor<Long>() {

            @Override
            public Long visit(All filter) {
                try {
                    return Long.valueOf(UserDao.this.allRowsIndex.count(context));
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Long visit(ByEnabledStatus filter) {
                try {
                    Boolean indexKey = filter.getEnabledStatusCondition().getValue();
                    // UNEQUAL is not exposed by the filter.
                    return Long.valueOf(UserDao.this.enabledIndex.count(indexKey, context));
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        long count = 0;
        try {
            count = nonnull(userFilter).accept(visitor).longValue();
        }
        catch (RuntimeException e) {
            rethrow(e);
        }
        return count;
    }

    private static UserFilter nonnull(UserFilter filter) {
        if (filter != null) {
            return filter;
        }
        return UserFilter.filterAll();
    }

    private static class UserConverter implements BidirectionalConverter<User, CassandraRow<Username, String>> {

        @Override
        public CassandraRow<Username, String> convert(User source) {
            CassandraRow<Username, String> row = new CassandraRow<Username, String>(source.getId().getValue());
            row.setColumn(new StringColumn<String>(PASSWORD, source.getPassword() != null ? source.getPassword()
                    .getValue() : null));
            row.setColumn(new StringColumn<String>(EMAIL, SerializableValueType.toValue(source.getEmail())));
            row.setColumn(new StringColumn<String>(DESCRIPTION, source.getDescription()));
            row.setColumn(new BooleanColumn<String>(ENABLED, Boolean.valueOf(source.isEnabled())));
            return row;
        }

        @Override
        public User restore(CassandraRow<Username, String> target) throws IllegalArgumentException {
            String strPassword = (String) target.getColumn(PASSWORD).getValue();
            String strEmail = (String) target.getColumn(EMAIL).getValue();

            Password password = strPassword != null ? Password.valueOf(strPassword) : null;
            Email email = strEmail != null ? Email.valueOf(strEmail) : null;
            String description = (String) target.getColumn(DESCRIPTION).getValue();
            Boolean enabled = (Boolean) target.getColumn(ENABLED).getValue();

            Id<User, Username> id = Id.valueOf(target.getKey());
            User user = new User(id);
            user.setPassword(password);
            user.setEmail(email);
            user.setDescription(description);
            user.setEnabled(enabled.booleanValue());
            return user;
        }
    }

    private static class DenormalizedData {

        private String password;
        private String email;
        private String description;
        private boolean enabled;

        public DenormalizedData(String password, String email, String description, boolean enabled) {
            this.password = password;
            this.email = email;
            this.description = description;
            this.enabled = enabled;
        }

        public String getPassword() {
            return this.password;
        }

        public String getEmail() {
            return this.email;
        }

        public String getDescription() {
            return this.description;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        @Override
        public String toString() {
            return ObjectToStringConverter.toString(this, Property.valueOf("password", "****"),
                    Property.valueOf("email", this.email), Property.valueOf("description", this.description),
                    Property.valueOf("enabled", Boolean.valueOf(this.enabled)));
        }
    }

    private static class DenormalizedDataSerializer implements CompositeTypeSerializer<DenormalizedData> {

        @Override
        public List<Component<DenormalizedData, ?>> serialize(DenormalizedData compositeValue) {
            List<Component<DenormalizedData, ?>> components = new ArrayList<Component<DenormalizedData, ?>>();
            components
                    .add(new Component<DenormalizedData, String>(BasicType.STRING_UTF8, compositeValue.getPassword()));
            components.add(new Component<DenormalizedData, String>(BasicType.STRING_UTF8, compositeValue.getEmail()));
            components.add(new Component<DenormalizedData, String>(BasicType.STRING_UTF8, compositeValue
                    .getDescription()));
            components.add(new Component<DenormalizedData, Boolean>(BasicType.BOOLEAN, Boolean.valueOf(compositeValue
                    .isEnabled())));
            return components;
        }

        @Override
        public DenormalizedData deserialize(List<Component<DenormalizedData, ?>> components) {
            String password = (String) components.get(0).getValue();
            String email = (String) components.get(1).getValue();
            String description = (String) components.get(2).getValue();
            Boolean isEnabled = (Boolean) components.get(3).getValue();
            return new DenormalizedData(password, email, description, isEnabled.booleanValue());
        }
    }

    private static class DenormalizedDataConverter implements Converter<Column<String, DenormalizedData>, User> {

        public static DenormalizedData toDenormalizedData(User user) {
            return new DenormalizedData(user.getPassword() != null ? user.getPassword().getValue() : null,
                    SerializableValueType.toValue(user.getEmail()), user.getDescription(), user.isEnabled());
        }

        @Override
        public User convert(Column<String, DenormalizedData> indexedColumn) {
            Username userName = Username.valueOf(indexedColumn.getName().getValue());
            DenormalizedData denormalizedData = indexedColumn.getValue();

            Password password = denormalizedData.getPassword() != null ? Password.valueOf(denormalizedData
                    .getPassword()) : null;
            Email email = denormalizedData.getEmail() != null ? Email.valueOf(denormalizedData.getEmail()) : null;
            String description = denormalizedData.getDescription();
            boolean enabled = denormalizedData.isEnabled();

            Id<User, Username> id = Id.valueOf(userName);
            User user = new User(id);
            user.setPassword(password);
            user.setEmail(email);
            user.setDescription(description);
            user.setEnabled(enabled);
            return user;
        }
    }
}
