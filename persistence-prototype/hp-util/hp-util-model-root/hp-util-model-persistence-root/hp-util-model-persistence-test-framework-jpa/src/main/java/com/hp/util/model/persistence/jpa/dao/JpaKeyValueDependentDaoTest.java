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
import com.hp.util.common.model.Dependent;
import com.hp.util.common.type.Id;
import com.hp.util.model.persistence.dao.KeyValueDependentDao;
import com.hp.util.model.persistence.dao.KeyValueDependentDaoTest;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.JpaTestDataStoreProvider;

/**
 * Integration test for JPA {@link KeyValueDependentDao} implementations.
 * 
 * @param <I> type of the identifiable object's id
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <E> type of the owner's id
 * @param <O> type of the owner (the independent identifiable object)
 * @param <D> type of the DAO to test
 * @author Fabiel Zuniga
 */
public abstract class JpaKeyValueDependentDaoTest<I extends Serializable, T extends Identifiable<? super T, I> & Dependent<Id<O, E>>, E extends Serializable, O extends Identifiable<? super O, E>, D extends KeyValueDependentDao<I, T, E, O, JpaContext>>
        extends KeyValueDependentDaoTest<I, T, E, O, JpaContext, D> {

    /**
     * Creates a JPA DAO test.
     */
    public JpaKeyValueDependentDaoTest() {
        super(JpaTestDataStoreProvider.getDataStore());
    }
}
