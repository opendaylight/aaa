/*
 * Copyright (c) 2014, 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Test;
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
        when(connectionMock.isClosed()).thenReturn(false);
        DatabaseMetaData dbmMock = mock(DatabaseMetaData.class);
        when(connectionMock.getMetaData()).thenReturn(dbmMock);
        ResultSet rsUserMock = mock(ResultSet.class);
        when(dbmMock.getTables(null, null, "GRANTS", tableTypes)).thenReturn(rsUserMock);
        when(rsUserMock.next()).thenReturn(true);

        PreparedStatement pstmtMock = mock(PreparedStatement.class);
        when(connectionMock.prepareStatement(anyString())).thenReturn(pstmtMock);

        ResultSet rsMock = getMockedResultSet();
        when(pstmtMock.executeQuery()).thenReturn(rsMock);

        // Run Test
        Grants grants = grantStoreUnderTest.getGrants(did, uid);

        // Verify
        assertEquals(1, grants.getGrants().size());
        verify(pstmtMock).close();
    }

    public ResultSet getMockedResultSet() throws SQLException {
        ResultSet rsMock = mock(ResultSet.class);
        when(rsMock.next()).thenReturn(true).thenReturn(false);
        when(rsMock.getInt(GrantStore.COL_ID)).thenReturn(1);
        when(rsMock.getString(GrantStore.COL_TENANTID)).thenReturn(did);
        when(rsMock.getString(GrantStore.COL_USERID)).thenReturn(uid);
        when(rsMock.getString(GrantStore.COL_ROLEID)).thenReturn("Role_1");

        return rsMock;
    }

}
