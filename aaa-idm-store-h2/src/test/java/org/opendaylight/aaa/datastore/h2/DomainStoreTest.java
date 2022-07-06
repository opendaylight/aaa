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
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DomainStoreTest {
    private final Connection connectionMock = mock(Connection.class);
    private final DomainStore domainStoreUnderTest = new DomainStore(() -> connectionMock);

    @Test
    public void getDomainsTest() throws Exception {
        // Setup Mock Behavior
        DatabaseMetaData dbmMock = mock(DatabaseMetaData.class);
        when(connectionMock.getMetaData()).thenReturn(dbmMock);
        ResultSet rsUserMock = mock(ResultSet.class);
        when(dbmMock.getTables(null, null, DomainStore.TABLE, AbstractStore.TABLE_TYPES)).thenReturn(rsUserMock);
        when(rsUserMock.next()).thenReturn(true);

        Statement stmtMock = mock(Statement.class);
        when(connectionMock.createStatement()).thenReturn(stmtMock);

        ResultSet rsMock = getMockedResultSet();
        when(stmtMock.executeQuery(anyString())).thenReturn(rsMock);

        // Run Test
        Domains domains = domainStoreUnderTest.getDomains();

        // Verify
        assertEquals(1, domains.getDomains().size());
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

    private static ResultSet getMockedResultSet() throws SQLException {
        ResultSet rsMock = mock(ResultSet.class);
        when(rsMock.next()).thenReturn(true).thenReturn(false);
        when(rsMock.getString(DomainStore.COL_ID)).thenReturn("1");
        when(rsMock.getString(DomainStore.COL_NAME)).thenReturn("DomainName_1");
        when(rsMock.getString(DomainStore.COL_DESC)).thenReturn("Desc_1");
        when(rsMock.getBoolean(DomainStore.COL_ENABLED)).thenReturn(true);
        return rsMock;
    }
}
