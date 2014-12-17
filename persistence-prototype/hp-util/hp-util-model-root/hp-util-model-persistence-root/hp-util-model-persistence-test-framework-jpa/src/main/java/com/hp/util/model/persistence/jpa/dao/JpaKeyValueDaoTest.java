/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao;

import java.io.Serializable;

import com.hp.util.common.Identifiable;
import com.hp.util.model.persistence.dao.KeyValueDao;
import com.hp.util.model.persistence.dao.KeyValueDaoTest;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.JpaTestDataStoreProvider;

/**
 * Integration test for JPA {@link KeyValueDao} implementations.
 * 
 * @param <I> type of the identifiable object's id
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <D> type of the DAO to test
 * @author Fabiel Zuniga
 */
public abstract class JpaKeyValueDaoTest<I extends Serializable, T extends Identifiable<? super T, I>, D extends KeyValueDao<I, T, JpaContext>>
        extends KeyValueDaoTest<I, T, JpaContext, D> {

    /**
     * Creates a JPA DAO test.
     */
    public JpaKeyValueDaoTest() {
        super(JpaTestDataStoreProvider.getDataStore());
    }
}
