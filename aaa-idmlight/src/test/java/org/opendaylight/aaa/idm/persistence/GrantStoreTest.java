/*
 * Copyright (c) 2014-2015 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm.persistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.aaa.idm.model.Grants;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GrantStoreTest {

    Connection connectionMock = mock(Connection.class);
    private final GrantStore GrantStoreUnderTest = new GrantStore();
    private Long did=(long) 5;
    private Long uid=(long) 5;


    @Before
    public void setup() {
        GrantStoreUnderTest.dbConnection = connectionMock;
    }

    @After
    public void teardown() {
        //dts.destroy();
    }

    @Test
    public void getGrantsTest() throws SQLException, Exception {
        //Setup Mock Behavior
        Mockito.when(connectionMock.isClosed()).thenReturn(false);
        DatabaseMetaData dbmMock = mock(DatabaseMetaData.class);
        Mockito.when(connectionMock.getMetaData()).thenReturn(dbmMock);
        ResultSet rsUserMock = mock(ResultSet.class);
        Mockito.when(dbmMock.getTables(null,null,"GRANTS",null)).thenReturn(rsUserMock);
        Mockito.when(rsUserMock.next()).thenReturn(true);

        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Mockito.when(connectionMock.prepareStatement(any(String.class))).thenReturn(stmtMock);

        ResultSet rsMock = getMockedResultSet();
        Mockito.when(stmtMock.executeQuery()).thenReturn(rsMock);

        //Run Test
        Grants grants = GrantStoreUnderTest.getGrants(did,uid);

        //Verify
        assertTrue(grants.getGrants().size() == 1);
        verify(stmtMock).close();
        }

    public ResultSet getMockedResultSet(){
        ResultSet rsMock = mock(ResultSet.class);
        try {
            Mockito.when(rsMock.next()).thenReturn(true).thenReturn(false);
            Mockito.when(rsMock.getInt(GrantStore.SQL_ID)).thenReturn(1);
            Mockito.when(rsMock.getString(GrantStore.SQL_DESCR)).thenReturn("RoleofTenantUser_1");
            Mockito.when(rsMock.getLong(GrantStore.SQL_TENANTID)).thenReturn(did);
            Mockito.when(rsMock.getLong(GrantStore.SQL_USERID)).thenReturn(uid);
            Mockito.when(rsMock.getString(GrantStore.SQL_ROLEID)).thenReturn("Role_1");
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return rsMock;
        }

}


