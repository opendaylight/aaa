/*
 * Copyright (c) 2014, 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.aaa.api.model.Grants;

public class GrantStoreTest {

    private final Connection connectionMock = mock(Connection.class);

    private final ConnectionProvider connectionFactoryMock = () -> connectionMock;

    private final GrantStore grantStoreUnderTest = new GrantStore(connectionFactoryMock);

    private final String did = "5";
    private final String uid = "5";

    @Test
    public void getGrantsTest() throws Exception {
        // Setup Mock Behavior
        String[] tableTypes = { "TABLE" };
        Mockito.when(connectionMock.isClosed()).thenReturn(false);
        DatabaseMetaData dbmMock = mock(DatabaseMetaData.class);
        Mockito.when(connectionMock.getMetaData()).thenReturn(dbmMock);
        ResultSet rsUserMock = mock(ResultSet.class);
        Mockito.when(dbmMock.getTables(null, null, "GRANTS", tableTypes)).thenReturn(rsUserMock);
        Mockito.when(rsUserMock.next()).thenReturn(true);

        PreparedStatement pstmtMock = mock(PreparedStatement.class);
        Mockito.when(connectionMock.prepareStatement(anyString())).thenReturn(pstmtMock);

        ResultSet rsMock = getMockedResultSet();
        Mockito.when(pstmtMock.executeQuery()).thenReturn(rsMock);

        // Run Test
        Grants grants = grantStoreUnderTest.getGrants(did, uid);

        // Verify
        assertTrue(grants.getGrants().size() == 1);
        verify(pstmtMock).close();
    }

    public ResultSet getMockedResultSet() throws SQLException {
        ResultSet rsMock = mock(ResultSet.class);
        Mockito.when(rsMock.next()).thenReturn(true).thenReturn(false);
        Mockito.when(rsMock.getInt(GrantStore.SQL_ID)).thenReturn(1);
        Mockito.when(rsMock.getString(GrantStore.SQL_TENANTID)).thenReturn(did);
        Mockito.when(rsMock.getString(GrantStore.SQL_USERID)).thenReturn(uid);
        Mockito.when(rsMock.getString(GrantStore.SQL_ROLEID)).thenReturn("Role_1");

        return rsMock;
    }

}
