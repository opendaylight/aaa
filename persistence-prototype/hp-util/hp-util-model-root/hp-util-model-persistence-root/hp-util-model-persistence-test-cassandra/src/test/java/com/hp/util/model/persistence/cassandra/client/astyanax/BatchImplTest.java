/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.model.persistence.cassandra.Batch;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.hp.util.model.persistence.cassandra.CassandraRow;
import com.hp.util.model.persistence.cassandra.CassandraTestUtil;
import com.hp.util.model.persistence.cassandra.column.Column;
import com.hp.util.model.persistence.cassandra.column.ColumnName;
import com.hp.util.model.persistence.cassandra.column.ColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.SameTypeColumnValueTypeProvider;
import com.hp.util.model.persistence.cassandra.column.StringColumn;
import com.hp.util.model.persistence.cassandra.keyspace.BasicType;
import com.hp.util.model.persistence.cassandra.keyspace.ColumnFamily;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings({ "javadoc", "static-method" })
public class BatchImplTest {

    protected static final ColumnFamily<String, String> TEST_COLUMN_FAMILY = new ColumnFamily<String, String>(
            "cf_test_batch", BasicType.STRING_UTF8, BasicType.STRING_UTF8,
            "Column family to test batch operations using AstyanaxFacade");

    private static final String ROW_KEY_1 = "row key 1";
    private static final String ROW_KEY_2 = "row key 2";
    private static final String ROW_KEY_3 = "row key 3";

    private static final ColumnName<String, String> COLUMN_NAME_1 = ColumnName.valueOf("column name 1");
    private static final ColumnName<String, String> COLUMN_NAME_2 = ColumnName.valueOf("column name 2");

    private static final Column<String, String> COLUMN_1_ROW_1 = new StringColumn<String>(COLUMN_NAME_1,
            "Hello World 1");
    private static final Column<String, String> COLUMN_1_ROW_2 = new StringColumn<String>(COLUMN_NAME_1,
            "Hello World 2");
    private static final Column<String, String> COLUMN_1_ROW_3 = new StringColumn<String>(COLUMN_NAME_1,
            "Hello World 2");

    private static final Column<String, String> COLUMN_2_ROW_1 = new StringColumn<String>(COLUMN_NAME_2, "Some data 1");
    private static final Column<String, String> COLUMN_2_ROW_2 = new StringColumn<String>(COLUMN_NAME_2, "Some data 2");
    private static final Column<String, String> COLUMN_2_ROW_3 = new StringColumn<String>(COLUMN_NAME_2, "Some data 3");

    private static final ColumnValueTypeProvider<String> COLUMN_VALUE_TYPE_PROVIDER = new SameTypeColumnValueTypeProvider<String, String>(
            BasicType.STRING_UTF8);

    @BeforeClass
    public static void beforeClass() throws Exception {
        CassandraTestUtil.beforeTestClass();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        CassandraTestUtil.afterTestClass();
    }

    @Before
    public void beforeTest() throws Exception {
        Assume.assumeTrue(CassandraTestUtil.isIntegrationTestSupported());

        CassandraTestUtil.beforeTest();

        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                context.getCassandraClient().createColumnFamily(TEST_COLUMN_FAMILY, context.getKeyspace(), context);
                return null;
            }
        });
    }

    @After
    public void afterTest() throws Exception {
        if (!CassandraTestUtil.isIntegrationTestSupported()) {
            return;
        }

        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                context.getCassandraClient().dropColumnFamily(TEST_COLUMN_FAMILY, context.getKeyspace(), context);
                return null;
            }
        });

        CassandraTestUtil.afterTest();
    }

    @Test
    public void testInsertColumnsBatch() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                testInsertColumnsBatch(context);
                return null;
            }
        });
    }

    @Test
    public void testInsertRowsBatch() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                testInsertRowsBatch(context);
                return null;
            }
        });
    }

    @Test
    public void testDeleteColumnsBatch() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                testDeleteColumnsBatch(context);
                return null;
            }
        });
    }

    @Test
    public void testDeleteRowsBatch() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                testDeleteRowsBatch(context);
                return null;
            }
        });
    }

    @Test
    public void testOperationsCombinationBatch() throws Exception {
        CassandraTestUtil.execute(new Query<Void, CassandraContext<Astyanax>>() {
            @Override
            public Void execute(CassandraContext<Astyanax> context) throws PersistenceException {
                testOperationsCombinationBatch(context);
                return null;
            }
        });
    }

    protected void testInsertColumnsBatch(CassandraContext<Astyanax> context) throws PersistenceException {
        // Prepares test data
        final CassandraRow<String, String> row1 = new CassandraRow<String, String>(ROW_KEY_1);
        final CassandraRow<String, String> row2 = new CassandraRow<String, String>(ROW_KEY_2);
        final CassandraRow<String, String> row3 = new CassandraRow<String, String>(ROW_KEY_3);

        row1.setColumn(COLUMN_1_ROW_1);
        row2.setColumn(COLUMN_1_ROW_2);
        row3.setColumn(COLUMN_1_ROW_3);

        // Adds some initial data
        context.getCassandraClient().insert(row1, TEST_COLUMN_FAMILY, context);
        context.getCassandraClient().insert(row2, TEST_COLUMN_FAMILY, context);
        context.getCassandraClient().insert(row3, TEST_COLUMN_FAMILY, context);

        // Prepares the batch
        Batch<Astyanax> batch = context.getCassandraClient().prepareBatch(context);
        CassandraContext<Astyanax> batchContext = batch.start();

        // Records write operations
        context.getCassandraClient().insert(COLUMN_2_ROW_1, ROW_KEY_1, TEST_COLUMN_FAMILY, batchContext);
        context.getCassandraClient().insert(COLUMN_2_ROW_2, ROW_KEY_2, TEST_COLUMN_FAMILY, batchContext);
        context.getCassandraClient().insert(COLUMN_2_ROW_2, ROW_KEY_3, TEST_COLUMN_FAMILY, batchContext);

        // Verifies operations have not been executed

        // -------------------
        // 'batchContext' my be also used on read operations, and we used it once jut to show
        // it is possible. However we will use 'context' for read operations because it is a
        // better practice to use 'batchContext' just on write operations. Once the batch
        // has been executed 'batchContext' should no longer be used. It is not recommended to
        // keep the context after executing the batch because it could mistakenly be used on
        // write operations.

        CassandraRow<String, String> readRow = context.getCassandraClient().read(ROW_KEY_1, TEST_COLUMN_FAMILY,
                COLUMN_VALUE_TYPE_PROVIDER,
                /*
                 * 'context' or 'batchContext' can be used here but it is recommended to use
                 * 'batchContext' just on write operations
                 */batchContext);
        // -------------------
        Assert.assertNotNull(readRow);
        Assert.assertEquals(1, readRow.getColumns().size());
        Assert.assertNull(readRow.getColumn(COLUMN_NAME_2));

        readRow = context.getCassandraClient().read(ROW_KEY_2, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(1, readRow.getColumns().size());
        Assert.assertNull(readRow.getColumn(COLUMN_NAME_2));

        readRow = context.getCassandraClient().read(ROW_KEY_3, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(1, readRow.getColumns().size());
        Assert.assertNull(readRow.getColumn(COLUMN_NAME_2));

        // Executes the batch
        batch.execute();

        // Verifies operations were executed
        readRow = context.getCassandraClient().read(ROW_KEY_1, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(2, readRow.getColumns().size());
        Assert.assertNotNull(readRow.getColumn(COLUMN_NAME_2));

        readRow = context.getCassandraClient().read(ROW_KEY_2, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(2, readRow.getColumns().size());
        Assert.assertNotNull(readRow.getColumn(COLUMN_NAME_2));

        readRow = context.getCassandraClient().read(ROW_KEY_3, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(2, readRow.getColumns().size());
        Assert.assertNotNull(readRow.getColumn(COLUMN_NAME_2));
    }

    protected void testInsertRowsBatch(CassandraContext<Astyanax> context) throws PersistenceException {
        // Prepares test data
        final CassandraRow<String, String> row1 = new CassandraRow<String, String>(ROW_KEY_1);
        final CassandraRow<String, String> row2 = new CassandraRow<String, String>(ROW_KEY_2);
        final CassandraRow<String, String> row3 = new CassandraRow<String, String>(ROW_KEY_3);

        row1.setColumn(COLUMN_1_ROW_1);
        row2.setColumn(COLUMN_1_ROW_2);
        row3.setColumn(COLUMN_1_ROW_3);

        // Adds some initial data
        context.getCassandraClient().insert(row3, TEST_COLUMN_FAMILY, context);

        /*------------------------------------------------------------------------------------------------------
         * Batch to insert rows
         ------------------------------------------------------------------------------------------------------*/

        // Prepares the batch
        Batch<Astyanax> batch = context.getCassandraClient().prepareBatch(context);
        CassandraContext<Astyanax> batchContext = batch.start();

        // Records write operations
        context.getCassandraClient().insert(row1, TEST_COLUMN_FAMILY, batchContext);
        context.getCassandraClient().insert(row2, TEST_COLUMN_FAMILY, batchContext);

        // Verifies operations have not been executed
        CassandraRow<String, String> readRow = context.getCassandraClient().read(ROW_KEY_1, TEST_COLUMN_FAMILY,
                COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNull(readRow);

        readRow = context.getCassandraClient().read(ROW_KEY_2, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNull(readRow);

        readRow = context.getCassandraClient().read(ROW_KEY_3, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER,
                batchContext);
        Assert.assertNotNull(readRow);

        // Executes the batch
        batch.execute();

        // Verifies operations were executed
        readRow = context.getCassandraClient().read(ROW_KEY_1, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(1, readRow.getColumns().size());

        readRow = context.getCassandraClient().read(ROW_KEY_2, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(1, readRow.getColumns().size());

        readRow = context.getCassandraClient().read(ROW_KEY_3, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(1, readRow.getColumns().size());
    }

    protected void testDeleteColumnsBatch(CassandraContext<Astyanax> context) throws PersistenceException {
        // Prepares test data
        final CassandraRow<String, String> row1 = new CassandraRow<String, String>(ROW_KEY_1);
        final CassandraRow<String, String> row2 = new CassandraRow<String, String>(ROW_KEY_2);
        final CassandraRow<String, String> row3 = new CassandraRow<String, String>(ROW_KEY_3);

        row1.setColumn(COLUMN_1_ROW_1);
        row2.setColumn(COLUMN_1_ROW_2);
        row3.setColumn(COLUMN_1_ROW_3);

        row1.setColumn(COLUMN_2_ROW_1);
        row2.setColumn(COLUMN_2_ROW_2);
        row3.setColumn(COLUMN_2_ROW_3);

        // Adds some initial data
        context.getCassandraClient().insert(row1, TEST_COLUMN_FAMILY, context);
        context.getCassandraClient().insert(row2, TEST_COLUMN_FAMILY, context);
        context.getCassandraClient().insert(row3, TEST_COLUMN_FAMILY, context);

        // Prepares the batch
        Batch<Astyanax> batch = context.getCassandraClient().prepareBatch(context);
        CassandraContext<Astyanax> batchContext = batch.start();

        // Records write operations
        context.getCassandraClient().delete(COLUMN_NAME_1, ROW_KEY_1, TEST_COLUMN_FAMILY, batchContext);
        context.getCassandraClient().delete(COLUMN_NAME_2, ROW_KEY_2, TEST_COLUMN_FAMILY, batchContext);
        context.getCassandraClient().delete(COLUMN_NAME_1, ROW_KEY_3, TEST_COLUMN_FAMILY, batchContext);

        // Verifies operations have not been executed
        CassandraRow<String, String> readRow = context.getCassandraClient().read(ROW_KEY_1, TEST_COLUMN_FAMILY,
                COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(2, readRow.getColumns().size());

        readRow = context.getCassandraClient().read(ROW_KEY_2, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(2, readRow.getColumns().size());

        readRow = context.getCassandraClient().read(ROW_KEY_3, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER,
                batchContext);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(2, readRow.getColumns().size());

        // Executes the batch
        batch.execute();

        // Verifies operations were executed
        readRow = context.getCassandraClient().read(ROW_KEY_1, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(1, readRow.getColumns().size());
        Assert.assertNotNull(readRow.getColumn(COLUMN_NAME_2));

        readRow = context.getCassandraClient().read(ROW_KEY_2, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(1, readRow.getColumns().size());
        Assert.assertNotNull(readRow.getColumn(COLUMN_NAME_1));

        readRow = context.getCassandraClient().read(ROW_KEY_3, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(1, readRow.getColumns().size());
        Assert.assertNotNull(readRow.getColumn(COLUMN_NAME_2));
    }

    protected void testDeleteRowsBatch(CassandraContext<Astyanax> context) throws PersistenceException {
        // Prepares test data
        final CassandraRow<String, String> row1 = new CassandraRow<String, String>(ROW_KEY_1);
        final CassandraRow<String, String> row2 = new CassandraRow<String, String>(ROW_KEY_2);
        final CassandraRow<String, String> row3 = new CassandraRow<String, String>(ROW_KEY_3);

        row1.setColumn(COLUMN_1_ROW_1);
        row2.setColumn(COLUMN_1_ROW_2);
        row3.setColumn(COLUMN_1_ROW_3);

        // Adds some initial data
        context.getCassandraClient().insert(row1, TEST_COLUMN_FAMILY, context);
        context.getCassandraClient().insert(row2, TEST_COLUMN_FAMILY, context);
        context.getCassandraClient().insert(row3, TEST_COLUMN_FAMILY, context);

        // Prepares the batch
        Batch<Astyanax> batch = context.getCassandraClient().prepareBatch(context);
        CassandraContext<Astyanax> batchContext = batch.start();

        // Records write operations
        context.getCassandraClient().delete(ROW_KEY_1, TEST_COLUMN_FAMILY, batchContext);
        context.getCassandraClient().delete(Arrays.asList(ROW_KEY_2, ROW_KEY_3), TEST_COLUMN_FAMILY, batchContext);

        // Verifies operations have not been executed
        CassandraRow<String, String> readRow = context.getCassandraClient().read(ROW_KEY_1, TEST_COLUMN_FAMILY,
                COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);

        readRow = context.getCassandraClient().read(ROW_KEY_2, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);

        readRow = context.getCassandraClient().read(ROW_KEY_3, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);

        // Executes the batch
        batch.execute();

        // Verifies operations were executed
        readRow = context.getCassandraClient().read(ROW_KEY_1, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNull(readRow);

        readRow = context.getCassandraClient().read(ROW_KEY_2, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNull(readRow);

        readRow = context.getCassandraClient().read(ROW_KEY_3, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNull(readRow);
    }

    protected void testOperationsCombinationBatch(CassandraContext<Astyanax> context) throws PersistenceException {
        // Prepares the batch
        Batch<Astyanax> batch = context.getCassandraClient().prepareBatch(context);
        CassandraContext<Astyanax> batchContext = batch.start();

        // Records write operations
        CassandraRow<String, String> row1 = new CassandraRow<String, String>(ROW_KEY_1);
        row1.setColumn(COLUMN_1_ROW_1);
        row1.setColumn(COLUMN_2_ROW_1);

        context.getCassandraClient().insert(row1, TEST_COLUMN_FAMILY, batchContext);
        context.getCassandraClient().insert(COLUMN_1_ROW_2, ROW_KEY_2, TEST_COLUMN_FAMILY, batchContext);
        context.getCassandraClient().insert(COLUMN_2_ROW_2, ROW_KEY_2, TEST_COLUMN_FAMILY, batchContext);
        context.getCassandraClient().insert(COLUMN_1_ROW_3, ROW_KEY_3, TEST_COLUMN_FAMILY, batchContext);
        context.getCassandraClient().delete(COLUMN_NAME_2, ROW_KEY_2, TEST_COLUMN_FAMILY, batchContext);
        context.getCassandraClient().delete(ROW_KEY_3, TEST_COLUMN_FAMILY, batchContext);

        // Verifies operations have not been executed
        CassandraRow<String, String> readRow = context.getCassandraClient().read(ROW_KEY_1, TEST_COLUMN_FAMILY,
                COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNull(readRow);

        readRow = context.getCassandraClient().read(ROW_KEY_2, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNull(readRow);

        readRow = context.getCassandraClient().read(ROW_KEY_3, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER,
                batchContext);
        Assert.assertNull(readRow);

        // Executes the batch
        batch.execute();

        // Verifies operations were executed
        readRow = context.getCassandraClient().read(ROW_KEY_1, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(2, readRow.getColumns().size());
        Assert.assertNotNull(readRow.getColumn(COLUMN_NAME_1));
        Assert.assertNotNull(readRow.getColumn(COLUMN_NAME_2));

        readRow = context.getCassandraClient().read(ROW_KEY_2, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNotNull(readRow);
        Assert.assertEquals(1, readRow.getColumns().size());
        Assert.assertNotNull(readRow.getColumn(COLUMN_NAME_1));

        readRow = context.getCassandraClient().read(ROW_KEY_3, TEST_COLUMN_FAMILY, COLUMN_VALUE_TYPE_PROVIDER, context);
        Assert.assertNull(readRow);
    }
}
