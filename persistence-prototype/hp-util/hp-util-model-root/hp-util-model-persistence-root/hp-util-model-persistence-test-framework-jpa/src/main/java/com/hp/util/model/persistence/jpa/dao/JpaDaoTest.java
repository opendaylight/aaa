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
import com.hp.util.model.persistence.dao.Dao;
import com.hp.util.model.persistence.dao.DaoTest;
import com.hp.util.model.persistence.jpa.JpaContext;
import com.hp.util.model.persistence.jpa.JpaTestDataStoreProvider;

/**
 * Integration test for JPA {@link Dao} implementations.
 * 
 * @param <I> type of the identifiable object's id
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <F> type of the associated filter
 * @param <S> type of the associated sort attribute or sort key used to construct sort
 *            specifications
 * @param <D> type of the DAO to test
 * @author Fabiel Zuniga
 */
public abstract class JpaDaoTest<I extends Serializable, T extends Identifiable<? super T, I>, F, S, D extends Dao<I, T, F, S, JpaContext>>
        extends DaoTest<I, T, F, S, JpaContext, D> {

    /**
     * Creates a JPA DAO test.
     */
    public JpaDaoTest() {
        super(JpaTestDataStoreProvider.getDataStore());
    }
}
