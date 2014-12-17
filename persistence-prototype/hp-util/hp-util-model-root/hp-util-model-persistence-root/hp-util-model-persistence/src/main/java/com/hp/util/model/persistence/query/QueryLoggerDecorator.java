/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.query;

import com.hp.util.common.log.Logger;
import com.hp.util.common.log.LoggerProvider;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;

/**
 * Decorator that logs information related to the execution of the query (Like the query's name and
 * the time it took to execute).
 * 
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @author Fabiel Zuniga
 */
public class QueryLoggerDecorator<T, C> implements Query<T, C> {

    private Query<T, C> delegate;
    private LoggerProvider<Class<?>> loggerProvider;

    /**
     * Creates a read query decorator.
     * 
     * @param delegate query delegate
     * @param loggerProvider logger provider
     */
    public QueryLoggerDecorator(Query<T, C> delegate, LoggerProvider<Class<?>> loggerProvider) {
        this.delegate = delegate;
        this.loggerProvider = loggerProvider;
    }

    @Override
    public T execute(C context) throws PersistenceException {
        Class<?> queryClass = this.delegate.getClass();

        Logger logger = this.loggerProvider.getLogger(queryClass);
        logger.info("Executing query " + queryClass.getSimpleName());

        T result = null;

        long startTime = System.currentTimeMillis();
        try {
            result = this.delegate.execute(context);
        }
        catch (Exception e) {
            logger.error("Failure executing query " + queryClass.getSimpleName(), e);
            throw e;
        }
        long endTime = System.currentTimeMillis();

        logger.info("Query " + queryClass.getSimpleName() + " generated result " + result + " in "
                + Long.valueOf(endTime - startTime) + " ms");

        return result;
    }
}
