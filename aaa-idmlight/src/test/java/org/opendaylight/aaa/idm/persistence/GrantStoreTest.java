/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm.persistence;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.aaa.idm.model.Grants;

public class GrantStoreTest {

    Connection connectionMock = mock(Connection.class);
    private final GrantStore grantStoreUnderTest = new GrantStore();
    private String did = "5";
    private String uid= "5";


    @Before
    public void setup() {
        grantStoreUnderTest.dbConnection = connectionMock;
    }

    @Test
    public void getGrantsTest() throws Exception {
        //Setup Mock Behavior
        Mockito.when(connectionMock.isClosed()).thenReturn(false);
        DatabaseMetaData dbmMock = mock(DatabaseMetaData.class);
        Mockito.when(connectionMock.getMetaData()).thenReturn(dbmMock);
        ResultSet rsUserMock = mock(ResultSet.class);
        Mockito.when(dbmMock.getTables(null,null,"GRANTS",null)).thenReturn(rsUserMock);
        Mockito.when(rsUserMock.next()).thenReturn(true);

        PreparedStatement pstmtMock = mock(PreparedStatement.class);
        Mockito.when(connectionMock.prepareStatement(anyString())).thenReturn(pstmtMock);

        ResultSet rsMock = getMockedResultSet();
        Mockito.when(pstmtMock.executeQuery()).thenReturn(rsMock);

        //Run Test
        Grants grants = grantStoreUnderTest.getGrants(did,uid);

        //Verify
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


