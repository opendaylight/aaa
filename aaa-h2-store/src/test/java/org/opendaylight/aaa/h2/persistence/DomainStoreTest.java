/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.h2.persistence;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.aaa.api.model.Domains;
import org.opendaylight.aaa.h2.config.ConnectionProvider;
import org.opendaylight.aaa.h2.persistence.DomainStore;

public class DomainStoreTest {

    private final Connection connectionMock = mock(Connection.class);

    private final ConnectionProvider connectionFactoryMock = () -> connectionMock;

    private final DomainStore domainStoreUnderTest = new DomainStore(connectionFactoryMock);

    @Test
    public void getDomainsTest() throws SQLException, Exception {
        // Setup Mock Behavior
        String[] tableTypes = { "TABLE" };
        Mockito.when(connectionMock.isClosed()).thenReturn(false);
        DatabaseMetaData dbmMock = mock(DatabaseMetaData.class);
        Mockito.when(connectionMock.getMetaData()).thenReturn(dbmMock);
        ResultSet rsUserMock = mock(ResultSet.class);
        Mockito.when(dbmMock.getTables(null, null, "DOMAINS", tableTypes)).thenReturn(rsUserMock);
        Mockito.when(rsUserMock.next()).thenReturn(true);

        Statement stmtMock = mock(Statement.class);
        Mockito.when(connectionMock.createStatement()).thenReturn(stmtMock);

        ResultSet rsMock = getMockedResultSet();
        Mockito.when(stmtMock.executeQuery(anyString())).thenReturn(rsMock);

        // Run Test
        Domains domains = domainStoreUnderTest.getDomains();

        // Verify
        assertTrue(domains.getDomains().size() == 1);
        verify(stmtMock).close();
    }

    public ResultSet getMockedResultSet() throws SQLException {
        ResultSet rsMock = mock(ResultSet.class);
        Mockito.when(rsMock.next()).thenReturn(true).thenReturn(false);
        Mockito.when(rsMock.getInt(DomainStore.SQL_ID)).thenReturn(1);
        Mockito.when(rsMock.getString(DomainStore.SQL_NAME)).thenReturn("DomainName_1");
        Mockito.when(rsMock.getString(DomainStore.SQL_DESCR)).thenReturn("Desc_1");
        Mockito.when(rsMock.getInt(DomainStore.SQL_ENABLED)).thenReturn(1);
        return rsMock;
    }
}
