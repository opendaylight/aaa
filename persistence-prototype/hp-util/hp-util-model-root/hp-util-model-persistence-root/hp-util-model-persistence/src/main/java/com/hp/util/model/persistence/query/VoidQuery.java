/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.query;

import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;

/**
 * Void query.
 * 
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Fabiel Zuniga
 */
public class VoidQuery<C> implements Query<Void, C> {

    /*
     * NOTE: There are several ways of implementing the singleton pattern, some of them more secure
     * than others guaranteeing that one and only one instance will exists in the system (taking
     * care of deserialization). However, the singleton pattern is used here to minimize the number
     * of instances of this class since all of them will behave the same. It is irrelevant if the
     * system ended up with more than one instance of this class.
     */
    @SuppressWarnings("rawtypes")
    private static final VoidQuery INSTANCE = new VoidQuery();

    private VoidQuery() {

    }

    /**
     * Gets the only instance of this class.
     * 
     * @return the only instance of this class
     */
    @SuppressWarnings("unchecked")
    public static <C> VoidQuery<C> getInstance() {
        return INSTANCE;
    }

    @Override
    public Void execute(C context) throws PersistenceException {
        return null;
    }
}
