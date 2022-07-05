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
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.aaa.api.model.Grants;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class GrantStoreTest {
    private static final String DOMAIN_ID = "5";
    private static final String USER_ID = "5";

    private final Connection connectionMock = mock(Connection.class);
    private final GrantStore grantStoreUnderTest = new GrantStore(() -> connectionMock);

    @Test
    public void getGrantsTest() throws Exception {
        // Setup Mock Behavior
        DatabaseMetaData dbmMock = mock(DatabaseMetaData.class);
        when(connectionMock.getMetaData()).thenReturn(dbmMock);
        ResultSet rsUserMock = mock(ResultSet.class);
        when(dbmMock.getTables(null, null, GrantStore.TABLE, AbstractStore.TABLE_TYPES)).thenReturn(rsUserMock);
        when(rsUserMock.next()).thenReturn(true);

        PreparedStatement pstmtMock = mock(PreparedStatement.class);
        when(connectionMock.prepareStatement(anyString())).thenReturn(pstmtMock);

        ResultSet rsMock = getMockedResultSet();
        when(pstmtMock.executeQuery()).thenReturn(rsMock);

        // Run Test
        Grants grants = grantStoreUnderTest.getGrants(DOMAIN_ID, USER_ID);

        // Verify
        assertEquals(1, grants.getGrants().size());
        verify(pstmtMock).close();
    }

    private static ResultSet getMockedResultSet() throws SQLException {
        ResultSet rsMock = mock(ResultSet.class);
        when(rsMock.next()).thenReturn(true).thenReturn(false);
        when(rsMock.getString(GrantStore.COL_ID)).thenReturn("1");
        when(rsMock.getString(GrantStore.COL_TENANTID)).thenReturn(DOMAIN_ID);
        when(rsMock.getString(GrantStore.COL_USERID)).thenReturn(USER_ID);
        when(rsMock.getString(GrantStore.COL_ROLEID)).thenReturn("Role_1");
        return rsMock;
    }
}
