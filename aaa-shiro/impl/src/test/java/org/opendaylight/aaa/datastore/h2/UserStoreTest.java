/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.impl.password.service.DefaultPasswordHashService;

public class UserStoreTest {

    private final Connection connectionMock = mock(Connection.class);

    private final ConnectionProvider connectionFactoryMock = () -> connectionMock;

    private final UserStore userStoreUnderTest = new UserStore(connectionFactoryMock,
            new DefaultPasswordHashService());

    @Test
    public void getUsersTest() throws SQLException, Exception {
        // Setup Mock Behavior
        String[] tableTypes = { "TABLE" };
        Mockito.when(connectionMock.isClosed()).thenReturn(false);
        DatabaseMetaData dbmMock = mock(DatabaseMetaData.class);
        Mockito.when(connectionMock.getMetaData()).thenReturn(dbmMock);
        ResultSet rsUserMock = mock(ResultSet.class);
        Mockito.when(dbmMock.getTables(null, null, "USERS", tableTypes)).thenReturn(rsUserMock);
        Mockito.when(rsUserMock.next()).thenReturn(true);

        Statement stmtMock = mock(Statement.class);
        Mockito.when(connectionMock.createStatement()).thenReturn(stmtMock);

        ResultSet rsMock = getMockedResultSet();
        Mockito.when(stmtMock.executeQuery(anyString())).thenReturn(rsMock);

        // Run Test
        Users users = userStoreUnderTest.getUsers();

        // Verify
        assertTrue(users.getUsers().size() == 1);
        verify(stmtMock).close();
    }

    public ResultSet getMockedResultSet() throws SQLException {
        ResultSet rsMock = mock(ResultSet.class);
        Mockito.when(rsMock.next()).thenReturn(true).thenReturn(false);
        Mockito.when(rsMock.getInt(UserStore.SQL_ID)).thenReturn(1);
        Mockito.when(rsMock.getString(UserStore.SQL_NAME)).thenReturn("Name_1");
        Mockito.when(rsMock.getString(UserStore.SQL_EMAIL)).thenReturn("Name_1@company.com");
        Mockito.when(rsMock.getString(UserStore.SQL_PASSWORD)).thenReturn("Pswd_1");
        Mockito.when(rsMock.getString(UserStore.SQL_DESCR)).thenReturn("Desc_1");
        Mockito.when(rsMock.getInt(UserStore.SQL_ENABLED)).thenReturn(1);
        return rsMock;
    }
}
