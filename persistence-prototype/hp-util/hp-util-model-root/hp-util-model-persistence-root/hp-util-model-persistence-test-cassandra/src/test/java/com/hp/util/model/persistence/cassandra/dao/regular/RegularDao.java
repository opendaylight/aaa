/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.dao.regular;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.hp.util.common.BidirectionalConverter;
import com.hp.util.common.type.Date;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.SortSpecification;
import com.hp.util.common.type.page.MarkPage;
import com.hp.util.common.type.page.MarkPageRequest;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraRow;
import com.hp.util.model.persistence.cassandra.MainColumnFamilyPrimitiveKey;
import com.hp.util.model.persistence.cassandra.column.BooleanColumn;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.DateColumn;
import com.hp.util.model.persistence.cassandra.column.DynamicColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.EnumColumn;
import com.hp.util.model.persistence.cassandra.column.LongColumn;
import com.hp.util.model.persistence.cassandra.column.StringColumn;
import com.hp.util.model.persistence.cassandra.dao.CassandraMarkPageDaoPrimitiveKey;
import com.hp.util.model.persistence.cassandra.dao.regular.RegularDtoFilter.All;
import com.hp.util.model.persistence.cassandra.dao.regular.RegularDtoFilter.ByAttributeBoolean;
import com.hp.util.model.persistence.cassandra.dao.regular.RegularDtoFilter.ByAttributeDate;
import com.hp.util.model.persistence.cassandra.dao.regular.RegularDtoFilter.ByAttributeEnum;
import com.hp.util.model.persistence.cassandra.dao.regular.RegularDtoFilter.ByAttributeEnumAndAttributeDate;
import com.hp.util.model.persistence.cassandra.dao.regular.RegularDtoFilter.ByAttributeLong;
import com.hp.util.model.persistence.cassandra.dao.regular.RegularDtoFilter.ByAttributeString;
import com.hp.util.model.persistence.cassandra.index.AllRowsSecondaryIndex;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily.SecondaryIndex;
import com.hp.util.model.persistence.cassandra.keyspace.DataType;
import com.hp.util.model.persistence.cassandra.keyspace.EnumType;
import com.hp.util.model.persistence.cassandra.mock.EnumMock;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class RegularDao<N> extends CassandraMarkPageDaoPrimitiveKey<String, RegularDto, RegularDtoFilter, Void, N> {

    /*
     * This DAO does not use any of the native features of the Cassandra Client, so it can be
     * parameterized with any Native Cassandra Client.
     */

    private static final ColumnName<String, String> ATTR_STRING = ColumnName.valueOf("attribute_string");
    private static final ColumnName<String, Boolean> ATTR_BOOLEAN = ColumnName.valueOf("attribute_boolean");
    private static final ColumnName<String, Long> ATTR_LONG = ColumnName.valueOf("attribute_long");
    private static final ColumnName<String, Date> ATTR_DATE = ColumnName.valueOf("attribute_date");
    private static final ColumnName<String, EnumMock> ATTR_ENUM = ColumnName.valueOf("attribute_enum");

    private static final SecondaryIndex ATTR_BOOLEAN_INDEX = new SecondaryIndex(ATTR_BOOLEAN, BasicType.BOOLEAN);

    private static final DataType<EnumMock> ENUM_TYPE = EnumType.valueOf(EnumMock.class);

    private AllRowsSecondaryIndex<String, Void> allRowsIndex = new AllRowsSecondaryIndex<String, Void>(
            "cf_regular_dao_all_rows_index", BasicType.STRING_UTF8, BasicType.VOID);

    /**
     * Creates a DAO.
     */
    public RegularDao() {
        super(new MainColumnFamilyPrimitiveKey<String, RegularDto>("cf_regular_dao", BasicType.STRING_UTF8,
                "Regular DAO main column family", createColumnValueTypeProvider(), new Converter(), ATTR_BOOLEAN_INDEX));
    }

    private static ColumnValueTypeProvider<String> createColumnValueTypeProvider() {
        DynamicColumnValueTypeProvider<String> columnValueTypeProvider = new DynamicColumnValueTypeProvider<String>();
        columnValueTypeProvider.registerColumnValueType(ATTR_STRING, BasicType.STRING_UTF8);
        columnValueTypeProvider.registerColumnValueType(ATTR_BOOLEAN, BasicType.BOOLEAN);
        columnValueTypeProvider.registerColumnValueType(ATTR_LONG, BasicType.LONG);
        columnValueTypeProvider.registerColumnValueType(ATTR_DATE, BasicType.DATE);
        columnValueTypeProvider.registerColumnValueType(ATTR_ENUM, ENUM_TYPE);
        return columnValueTypeProvider;
    }

    @Override
    protected Collection<ColumnFamily<?, ?>> getIndexesColumnFamilyDefinitions() {
        Collection<ColumnFamily<?, ?>> columnFamilyDefinitions = new ArrayList<ColumnFamily<?, ?>>();
        columnFamilyDefinitions.addAll(this.allRowsIndex.getColumnFamilies());
        return columnFamilyDefinitions;
    }

    @Override
    protected void updateIndexesBeforeInsertion(RegularDto identifiable, CassandraContext<N> context)
            throws PersistenceException {
        this.allRowsIndex.insert(identifiable.getId().getValue(), null, context);
    }

    @Override
    protected void updateIndexesBeforeDeletion(Id<RegularDto, String> id, CassandraContext<N> context)
            throws PersistenceException {
        this.allRowsIndex.delete(id.getValue(), context);
    }

    @Override
    public List<RegularDto> find(RegularDtoFilter regularDtoFilter, final SortSpecification<Void> sortSpecification,
            final CassandraContext<N> context) throws PersistenceException {
        RegularDtoFilter.Visitor<List<CassandraRow<String, String>>> visitor = new RegularDtoFilter.Visitor<List<CassandraRow<String, String>>>() {

            @Override
            public List<CassandraRow<String, String>> visit(All filter) {
                List<Column<String, Void>> ids;
                try {
                    ids = RegularDao.this.allRowsIndex.read(context);
                    return getMainColumnFamily().read(extractKeysFromIndexedColumns(ids), context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public List<CassandraRow<String, String>> visit(ByAttributeString filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<CassandraRow<String, String>> visit(ByAttributeBoolean filter) {
                try {
                    return context.getCassandraClient().searchWithIndex(ATTR_BOOLEAN,
                            filter.getAttributeBooleanCondition(), getMainColumnFamily().getColumnFamily(),
                            getMainColumnFamily().getColumnValueTypeProvider(), BasicType.BOOLEAN, context);
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public List<CassandraRow<String, String>> visit(ByAttributeLong filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<CassandraRow<String, String>> visit(ByAttributeDate filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<CassandraRow<String, String>> visit(ByAttributeEnum filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<CassandraRow<String, String>> visit(ByAttributeEnumAndAttributeDate filter) {
                // TODO Auto-generated method stub
                return null;
            }
        };

        List<CassandraRow<String, String>> rows = Collections.emptyList();
        try {
            rows = nonnull(regularDtoFilter).accept(visitor);
        }
        catch (RuntimeException e) {
            rethrow(e);
        }

        List<RegularDto> dtos = new ArrayList<RegularDto>(rows.size());
        for (CassandraRow<String, String> row : rows) {
            dtos.add(convert(row));
        }

        return dtos;
    }

    @Override
    public long count(RegularDtoFilter regularDtoFilter, final CassandraContext<N> context) throws PersistenceException {
        RegularDtoFilter.Visitor<Long> visitor = new RegularDtoFilter.Visitor<Long>() {

            @Override
            public Long visit(All filter) {
                try {
                    return Long.valueOf(RegularDao.this.allRowsIndex.count(context));
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Long visit(ByAttributeString filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Long visit(ByAttributeBoolean filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Long visit(ByAttributeLong filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Long visit(ByAttributeDate filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Long visit(ByAttributeEnum filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Long visit(ByAttributeEnumAndAttributeDate filter) {
                // TODO Auto-generated method stub
                return null;
            }

        };

        long count = 0;
        try {
            count = nonnull(regularDtoFilter).accept(visitor).longValue();
        }
        catch (RuntimeException e) {
            rethrow(e);
        }
        return count;
    }

    @Override
    public MarkPage<RegularDto> find(RegularDtoFilter regularDtoFilter,
            final SortSpecification<Void> sortSpecification, final MarkPageRequest<RegularDto> pageRequest,
            final CassandraContext<N> context) throws PersistenceException {
        if (pageRequest == null) {
            throw new NullPointerException("pageRequest cannot be null");
        }

        RegularDtoFilter.Visitor<MarkPage<CassandraRow<String, String>>> visitor = new RegularDtoFilter.Visitor<MarkPage<CassandraRow<String, String>>>() {

            @Override
            public MarkPage<CassandraRow<String, String>> visit(All filter) {
                try {
                    String id = pageRequest.getMark() != null ? pageRequest.getMark().getId().getValue() : null;
                    MarkPage<Column<String, Void>> indexedColumnsPage = RegularDao.this.allRowsIndex.read(
                            new MarkPageRequest<String>(id, pageRequest.getNavigation(), pageRequest.getSize()),
                            context);
                    CassandraRow<String, String> markRow = null;
                    if (pageRequest.getMark() != null) {
                        markRow = getMainColumnFamily().read(pageRequest.getMark().getId(), context);
                    }

                    /*
                     * indexedColumnsPage contains the ids and denormalized data (not used here). So
                     * we need to read the identifiable objects from the main column family.
                     */
                    List<CassandraRow<String, String>> rows = getMainColumnFamily().read(
                            extractKeysFromIndexedColumns(indexedColumnsPage.getData()), context);

                    return new MarkPage<CassandraRow<String, String>>(pageRequest.convert(markRow),
                            new ArrayList<CassandraRow<String, String>>(rows));
                }
                catch (PersistenceException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public MarkPage<CassandraRow<String, String>> visit(ByAttributeString filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public MarkPage<CassandraRow<String, String>> visit(ByAttributeBoolean filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public MarkPage<CassandraRow<String, String>> visit(ByAttributeLong filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public MarkPage<CassandraRow<String, String>> visit(ByAttributeDate filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public MarkPage<CassandraRow<String, String>> visit(ByAttributeEnum filter) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public MarkPage<CassandraRow<String, String>> visit(ByAttributeEnumAndAttributeDate filter) {
                // TODO Auto-generated method stub
                return null;
            }

        };

        MarkPage<CassandraRow<String, String>> page = MarkPage.emptyPage();
        try {
            page = nonnull(regularDtoFilter).accept(visitor);
        }
        catch (RuntimeException e) {
            rethrow(e);
        }

        return page.convert(this);
    }

    private static List<String> extractKeysFromIndexedColumns(List<Column<String, Void>> indexedColumns) {
        List<String> rowKeys = new ArrayList<String>(indexedColumns.size());
        for (Column<String, ?> column : indexedColumns) {
            rowKeys.add(column.getName().getValue());
        }
        return rowKeys;
    }

    private static RegularDtoFilter nonnull(RegularDtoFilter filter) {
        if (filter != null) {
            return filter;
        }
        return RegularDtoFilter.filterAll();
    }

    private static class Converter implements BidirectionalConverter<RegularDto, CassandraRow<String, String>> {

        @Override
        public CassandraRow<String, String> convert(RegularDto source) {
            CassandraRow<String, String> row = new CassandraRow<String, String>(source.getId().getValue());
            row.setColumn(new StringColumn<String>(ATTR_STRING, source.getAttributeString()));
            row.setColumn(new BooleanColumn<String>(ATTR_BOOLEAN, Boolean.valueOf(source.getAttributeBoolean())));
            row.setColumn(new LongColumn<String>(ATTR_LONG, source.getAttributeLong()));
            row.setColumn(new DateColumn<String>(ATTR_DATE, source.getAttributeDate()));
            row.setColumn(new EnumColumn<String, EnumMock>(ATTR_ENUM, source.getAttributeEnum()));

            return row;
        }

        @Override
        public RegularDto restore(CassandraRow<String, String> row) throws IllegalArgumentException {
            RegularDto dto = new RegularDto(Id.<RegularDto, String> valueOf(row.getKey()));
            dto.setAttributeBoolean(((Boolean) row.getColumn(ATTR_BOOLEAN).getValue()).booleanValue());
            dto.setAttributeDate(((Date) row.getColumn(ATTR_DATE).getValue()));
            dto.setAttributeLong(((Long) row.getColumn(ATTR_LONG).getValue()));
            dto.setAttributeString(((String) row.getColumn(ATTR_STRING).getValue()));
            dto.setAttributeEnum(((EnumMock) row.getColumn(ATTR_ENUM).getValue()));
            return dto;
        }
    }
}
