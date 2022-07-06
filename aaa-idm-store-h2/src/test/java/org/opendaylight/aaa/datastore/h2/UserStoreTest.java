/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.impl.password.service.DefaultPasswordHashService;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class UserStoreTest {
    private final Connection connectionMock = mock(Connection.class);
    private final UserStore userStoreUnderTest = new UserStore(() -> connectionMock,
            new DefaultPasswordHashService());

    @Test
    public void getUsersTest() throws Exception {
        // Setup Mock Behavior
        DatabaseMetaData dbmMock = mock(DatabaseMetaData.class);
        when(connectionMock.getMetaData()).thenReturn(dbmMock);
        ResultSet rsUserMock = mock(ResultSet.class);
        when(dbmMock.getTables(null, null, UserStore.TABLE, AbstractStore.TABLE_TYPES)).thenReturn(rsUserMock);
        when(rsUserMock.next()).thenReturn(true);

        Statement stmtMock = mock(Statement.class);
        when(connectionMock.createStatement()).thenReturn(stmtMock);

        ResultSet rsMock = getMockedResultSet();
        when(stmtMock.executeQuery(anyString())).thenReturn(rsMock);

        // Run Test
        Users users = userStoreUnderTest.getUsers();

        // Verify
        assertEquals(1, users.getUsers().size());
        verify(stmtMock).close();
    }

    private static ResultSet getMockedResultSet() throws SQLException {
        ResultSet rsMock = mock(ResultSet.class);
        when(rsMock.next()).thenReturn(true).thenReturn(false);
        when(rsMock.getString(UserStore.COL_ID)).thenReturn("1");
        when(rsMock.getString(UserStore.COL_NAME)).thenReturn("Name_1");
        when(rsMock.getString(UserStore.COL_EMAIL)).thenReturn("Name_1@company.com");
        when(rsMock.getString(UserStore.COL_PASSWORD)).thenReturn("Pswd_1");
        when(rsMock.getString(UserStore.COL_DESC)).thenReturn("Desc_1");
        when(rsMock.getString(UserStore.COL_DOMAIN_ID)).thenReturn("Domain_1");
        when(rsMock.getBoolean(UserStore.COL_ENABLED)).thenReturn(true);
        return rsMock;
    }
}
