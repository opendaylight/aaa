/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;

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

    @Test
    public void deleteDomainsTest() throws SQLException, Exception {
        String domainId = "Testing12345";

        // Run Test
        Domain testDomain = new Domain();
        testDomain.setDomainid(domainId);
        testDomain.setName(domainId);
        testDomain.setEnabled(Boolean.TRUE);

        DomainStore ds = new DomainStore(
                new IdmLightSimpleConnectionProvider(new IdmLightConfigBuilder().dbUser("foo").dbPwd("bar").build()));

        ds.createDomain(testDomain);
        assertEquals(ds.getDomain(domainId).getDomainid(), domainId);
        ds.deleteDomain(domainId);
        assertNull(ds.getDomain(domainId));
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
