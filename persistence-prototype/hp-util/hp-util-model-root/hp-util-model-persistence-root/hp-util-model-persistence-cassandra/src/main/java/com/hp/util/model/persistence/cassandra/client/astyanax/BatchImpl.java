/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import java.lang.reflect.Method;

import com.hp.util.common.proxy.DynamicProxy;
import com.hp.util.common.proxy.Proxy;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.cassandra.Batch;
import com.hp.util.model.persistence.cassandra.CassandraContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;

/**
 * @author Fabiel Zuniga
 */
class BatchImpl implements Batch<Astyanax> {
    private final MutationBatch mutationBatch;
    private final MutationBatch mutationBatchProxy;
    private final CassandraContext<Astyanax> batchContext;
    
    public BatchImpl(CassandraContext<Astyanax> context) {
        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }

        this.mutationBatch = context.getNativeClient().getKeyspace().prepareMutationBatch();
        // this.mutationBatch = context.getKeyspace().prepareMutationBatch().usingWriteAheadLog(...);
        this.mutationBatch.setConsistencyLevel(context.getWriteConsistencyLevel().toAstyanaxLevel());

        this.mutationBatchProxy = DynamicProxy.create(this.mutationBatch, new MutationBatchProxy());

        Keyspace keyspaceProxy = DynamicProxy.create(context.getNativeClient().getKeyspace(), new KeySpaceProxy(
                this.mutationBatchProxy));
        Astyanax nativeClient = new Astyanax(keyspaceProxy, context.getNativeClient().getCluster());
        this.batchContext = AstyanaxCassandraContextAccessor.getDefault().createCassandraContext(context.getKeyspace(),
                context.getCassandraClient(), nativeClient);
        this.batchContext.setReadConsistencyLevel(context.getReadConsistencyLevel());
        this.batchContext.setWriteConsistencyLevel(context.getWriteConsistencyLevel());
    }

    @Override
    public CassandraContext<Astyanax> start() {
        return this.batchContext;
    }

    @Override
    public void execute() throws PersistenceException {
        try {
            this.mutationBatch.execute();
        }
        catch (ConnectionException e) {
            throw new PersistenceException(e);
        }
    }

    private static class KeySpaceProxy implements Proxy<Keyspace> {

        private MutationBatch proxy;

        protected KeySpaceProxy(MutationBatch proxy) {
            this.proxy = proxy;
        }

        @Override
        public Object invoke(Keyspace delegate, Method method, Object[] args) throws Throwable {
            // prepareMutationBatch() returns the common mutationBatch.
            if (method.getName().equals("prepareMutationBatch")) {
                return this.proxy;
            }

            return method.invoke(delegate, args);
        }
    }

    private static class MutationBatchProxy implements Proxy<MutationBatch> {

        protected MutationBatchProxy() {

        }

        @Override
        public Object invoke(MutationBatch delegate, Method method, Object[] args) throws Throwable {
            // execute() and setConsistencyLevel(...) methods are ignored
            if (!method.getName().equals("execute") && !method.getName().equals("setConsistencyLevel")) {
                return method.invoke(delegate, args);
            }

            return null;
        }
    }
}
