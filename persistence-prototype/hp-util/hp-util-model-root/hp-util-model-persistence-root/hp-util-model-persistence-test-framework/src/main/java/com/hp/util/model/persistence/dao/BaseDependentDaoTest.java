/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.util.common.Identifiable;
import com.hp.util.common.model.Dependent;
import com.hp.util.common.type.Id;
import com.hp.util.common.type.tuple.Pair;
import com.hp.util.model.persistence.DataStore;
import com.hp.util.model.persistence.PersistenceException;
import com.hp.util.model.persistence.Query;
import com.hp.util.test.ThrowableTester;
import com.hp.util.test.ThrowableTester.Instruction;

/**
 * Integration test for {@link BaseDependentDao} implementations.
 * 
 * @param <I> type of the identifiable object's id
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <E> type of the owner's id
 * @param <O> type of the owner (the independent identifiable object)
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @param <D> type of the DAO to test
 * @author Fabiel Zuniga
 */
public abstract class BaseDependentDaoTest<I extends Serializable, T extends Identifiable<? super T, I> & Dependent<Id<O, E>>, E extends Serializable, O extends Identifiable<? super O, E>, C, D extends BaseDependentDao<I, T, E, O, C>> {

    /*
     * NOTE: Any conversion should be called inside the query's execute method (In a Unit of Work).
     * Otherwise Session Exceptions will be thrown for entities that use lazy loading.
     */

    private DataStore<C> dataStore;

    /**
     * Creates a new dependent DAO integration test.
     * 
     * @param dataStore data store
     */
    public BaseDependentDaoTest(DataStore<C> dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Method executed before running the class tests.
     * 
     * @throws Exception if errors occur
     */
    @BeforeClass
    public static void beforeClass() throws Exception {

    }

    /**
     * Method executed after running the class tests.
     * 
     * @throws Exception if errors occur
     */
    @AfterClass
    public static void afterClass() throws Exception {

    }

    /**
     * Method executed before running each test.
     * 
     * @throws Exception if any errors occur during execution
     */
    @Before
    public void beforeTest() throws Exception {
        clearThroughOwner();
    }

    /**
     * Method executed after running each test.
     * 
     * @throws Exception if any errors occur during execution
     */
    @Before
    public void afterTest() throws Exception {

    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testCreate() throws Exception {
        final T original = createIdentifiables(1).get(0);
        Pair<T, O> tuple = storeThroughOwner(original);
        T stored = tuple.getFirst();
        Assert.assertNotNull(stored);
        Assert.assertNotNull(stored.getId());
        assertEqualState(original, stored);
        Assert.assertEquals(1, size());
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testPrimaryKeyIntegrityConstraintViolation() throws Exception {
        /*
         * This test is run just in cases where the primary key is not auto-generated - natural
         * keys.
         */
        Assume.assumeTrue(isPrimaryKeyIntegrityConstraintViolationSupported());

        List<T> identifiables = createIdentifiables(2);
        final T original = identifiables.get(0);

        Pair<T, O> tuple = storeThroughOwner(original);
        T stored = tuple.getFirst();
        final T duplicated = createIdentifiable(stored.<T> getId());

        Instruction violationExecutor = new Instruction() {
            @Override
            public void execute() throws Throwable {
                storeThroughOwner(duplicated);
            }
        };

        // ThrowableTester.testThrows(RollbackException.class, violationExecutor);
        ThrowableTester.testThrowsAny(RuntimeException.class, violationExecutor);
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testUpdate() throws Exception {
        final T identifiable = createIdentifiables(1).get(0);

        Pair<T, O> tuple = storeThroughOwner(identifiable);
        final T original = tuple.getFirst();

        modify(original);

        execute(new DaoQuery<Void>() {
            @Override
            protected Void execute(D dao, C context) throws PersistenceException {
                dao.update(original, context);
                return null;
            }

        });

        T updated = execute(new DaoQuery<T>() {
            @Override
            protected T execute(D dao, C context) throws PersistenceException {
                return dao.get(original.<T> getId(), context);
            }
        });

        assertEqualState(original, updated);
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testUpdateInvalid() throws Exception {
        final T invalidIdentifiable = createIdentifiables(1).get(0);

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                BaseDependentDaoTest.this.execute(new DaoQuery<Void>() {
                    @Override
                    protected Void execute(D dao, C context) throws PersistenceException {
                        dao.update(invalidIdentifiable, context);
                        return null;
                    }
                });
            }
        });
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testUpdateWithVersionConflict() throws Exception {
        Assume.assumeTrue(isVersioned());

        final Pair<T, O> stored = storeThroughOwner(createIdentifiables(1).get(0));
        final T old = stored.getFirst();

        final T latest = execute(new DaoQuery<T>() {
            @Override
            protected T execute(D dao, C context) throws PersistenceException {
                return dao.get(old.<T> getId(), context);
            }
        });

        modify(latest);

        execute(new DaoQuery<Void>() {
            @Override
            protected Void execute(D dao, C context) throws PersistenceException {
                dao.update(latest, context);
                return null;
            }

        });

        modify(old);

        ThrowableTester.testThrows(IllegalStateException.class, new Instruction() {
            @Override
            public void execute() throws Throwable {
                BaseDependentDaoTest.this.execute(new DaoQuery<Void>() {

                    @Override
                    protected Void execute(D dao, C context) throws PersistenceException {
                        dao.update(old, context);
                        return null;
                    }
                });
            }
        });
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testGet() throws Exception {
        final T identifiable = createIdentifiables(1).get(0);
        final T original = storeThroughOwner(identifiable).getFirst();

        T retrieved = execute(new DaoQuery<T>() {
            @Override
            protected T execute(D dao, C context) throws PersistenceException {
                return dao.get(original.<T> getId(), context);
            }

        });

        assertEqualState(original, retrieved);
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testGetInvalid() throws Exception {
        final Id<T, I> invalidId = null;

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                BaseDependentDaoTest.this.execute(new DaoQuery<T>() {

                    @Override
                    protected T execute(D dao, C context) throws PersistenceException {
                        return dao.get(invalidId, context);
                    }
                });
            }
        });
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testExist() throws Exception {
        final T identifiable = createIdentifiables(1).get(0);
        final T stored = storeThroughOwner(identifiable).getFirst();

        Boolean exist = execute(new DaoQuery<Boolean>() {

            @Override
            protected Boolean execute(D dao, C context) throws PersistenceException {
                return Boolean.valueOf(dao.exist(stored.<T> getId(), context));
            }
        });

        Assert.assertTrue(exist.booleanValue());
    }

    /**
     * @throws Exception if any errors occur during execution
     */
    @Test
    public void testExistInvalid() throws Exception {
        final Id<T, I> invalidId = null;

        ThrowableTester.testThrows(NullPointerException.class, new Instruction() {

            @Override
            public void execute() throws Throwable {
                BaseDependentDaoTest.this.execute(new DaoQuery<Boolean>() {

                    @Override
                    protected Boolean execute(D dao, C context) throws PersistenceException {
                        return Boolean.valueOf(dao.exist(invalidId, context));
                    }
                });
            }
        });
    }

    /**
     * Executes a query.
     * 
     * @param query query to execute
     * @return result
     * @throws Exception if any errors occur during execution
     */
    protected <R> R execute(Query<R, C> query) throws Exception {
        return this.dataStore.execute(query);
    }

    /**
     * Stores the given collection of identifiables.
     * <p>
     * The objects contained in {@code identifiables} might not have an id if it is auto-generated,
     * so it is not possible to relate them to a persistent object for query result comparisons.
     * This method returns a structure where each object to store is paired to the persisted object
     * (in case they are different).
     * 
     * @param identifiables identifiables to store
     * @return persisted objects
     * @throws Exception if errors occur during execution
     */
    protected Map<I, StoredObject<T>> store(Collection<T> identifiables) throws Exception {
        Map<I, StoredObject<T>> persistedObjects = new HashMap<I, StoredObject<T>>();
        for (final T original : identifiables) {
            Pair<T, O> tuple = storeThroughOwner(original);
            T stored = tuple.getFirst();
            persistedObjects.put(stored.getId().getValue(), StoredObject.<T> valueOf(original, stored));
        }
        return persistedObjects;
    }

    /**
     * Compares the collection of {@code expected} and {@code actual} identifiables and verifies
     * that they have the same objects (Just compares their identity).
     * <p>
     * This method is useful to test the result of queries that load objects using some criteria.
     * 
     * @param expected expected (this objects may have a {@code null} id in case it is
     *            auto-generated)
     * @param actual actual
     * @param searchSpace search space
     * @param ordered {@code true} to consider the order of objects in both {@code expected} and
     *            {@code actual} (treat them as lists), {@code false} to only consider membership
     *            (treat them as sets)
     * @param errorMessage error message to include in case of failure
     */
    protected void assertSearch(List<T> expected, List<T> actual, Map<I, StoredObject<T>> searchSpace, boolean ordered,
            String errorMessage) {
        Assert.assertEquals(errorMessage, expected.size(), actual.size());

        if (ordered) {
            for (int i = expected.size() - 1; i >= 0; i--) {
                T expectedIdentifiable = expected.get(i);
                T actualStored = actual.get(i);
                T actualOriginal = searchSpace.get(actualStored.getId().getValue()).getOriginal();
                Assert.assertSame(errorMessage, expectedIdentifiable, actualOriginal);
            }
        }
        else {
            for (T actualStored : actual) {
                T actualIdentifiable = searchSpace.get(actualStored.getId().getValue()).getOriginal();
                boolean found = false;
                for (T identifiable : expected) {
                    if (identifiable == actualIdentifiable) {
                        found = true;
                        break;
                    }
                }
                Assert.assertTrue(errorMessage, found);
            }
        }
    }

    /**
     * Creates the instance of the DAO to test.
     * 
     * @return a new instance of the DAO to test
     */
    protected abstract D createDaoInstance();

    /**
     * Returns {@code true} to execute {@link #testPrimaryKeyIntegrityConstraintViolation()}.
     * Primary key integrity constraint violation should be tested if the primary key is not
     * auto-generated and if {@link BaseDao#create(Identifiable, Object)} should fail in case
     * duplicated keys. This test is common in relation models. In non-relational models or
     * Key-Value type models there is no difference between
     * {@link BaseDao#create(Identifiable, Object)} and {@link BaseDao#update(Identifiable, Object)}
     * , and thus {@link #testPrimaryKeyIntegrityConstraintViolation()} should be ignored.
     * 
     * @return {@code true} to execute {@link #testPrimaryKeyIntegrityConstraintViolation()},
     *         {@code false} to ignore it
     */
    protected abstract boolean isPrimaryKeyIntegrityConstraintViolationSupported();

    /**
     * Returns {@code true} to execute {@link #testUpdateWithVersionConflict()}. This method should
     * return {@code true} of the identifiable keeps a version field that makes the the update fail
     * if versions don't match.
     * 
     * @return {@code true} to execute {@link #testUpdateWithVersionConflict()}
     */
    protected abstract boolean isVersioned();

    /**
     * Removes all persisted objects (Including owners if any).
     * 
     * @throws Exception if any errors occur during execution
     */
    protected abstract void clearThroughOwner() throws Exception;

    /**
     * Creates a transfer object with the given id. This method is called to assist
     * {@link #testPrimaryKeyIntegrityConstraintViolation()} when
     * {@link #isPrimaryKeyIntegrityConstraintViolationSupported()} returns {@code true}.
     * 
     * @param id transfer object's id
     * @return a transfer object with the given id
     */
    protected abstract T createIdentifiable(Id<T, I> id);

    /**
     * Creates a collection of identifiable objects to use in a test case. They will represent the
     * content of the table for the test case. These objects must be a valid storable collection
     * since they will be persisted all together. So if an attribute is unique (unique column in the
     * data store), the implementation must make sure the field is unique for the returned objects.
     * The returned collection can be considered as the entire table content, so it is enough to
     * make sure unique fields are unique just for the returned objects; These objects will be
     * removed from the data store after the test.
     * <p>
     * Example:
     * 
     * <pre>
     * &#064;Override
     * protected List&lt;MyIdentifiable&gt; createIdentifiables(int count) {
     *     List&lt;MyIdentifiable&gt; identifiables = new ArrayList&lt;MyIdentifiable&gt;();
     *     for (int i = 0; i &lt; count; i++) {
     *         Id&lt;MyIdentifiable, Long&gt; id = Id.valueOf(Long.valueOf(i));
     *         identifiables.add(createIdentifiable(id));
     *     }
     *     return identifiables;
     * }
     * 
     * @param count number of objects to create
     * @return the objects
     */
    protected abstract List<T> createIdentifiables(int count);

    /**
     * Asserts that both objects have the same state. All attributes must be verified but the Id
     * (not just those attributes part of the equals method). Id should not be compared because
     * there is no guarantee the id will be valid. This method is used to compare created objects
     * (with no id) with retrieved objects from the data store.
     * 
     * @param expected expected
     * @param actual actual
     */
    protected abstract void assertEqualState(T expected, T actual);

    /**
     * Calculate the number of persisted objects.
     * 
     * @return the number of persisted objects
     * @throws Exception if any errors occur during execution
     */
    protected abstract long size() throws Exception;

    /**
     * Modifies the object with random valid data.
     * 
     * @param identifiable object to modify
     */
    protected abstract void modify(T identifiable);

    /**
     * Persists an object via its owner.
     * 
     * @param dependent object to persist
     * @return the persisted object as it is in the data store and its owner
     * @throws Exception if any errors occur during execution
     */
    protected abstract Pair<T, O> storeThroughOwner(final T dependent) throws Exception;

    /**
     * Query tied to the DAO being tested. Allows executing DAO instructions in the context of a
     * query, but those instructions are related a a single DAO - the one being tested.
     * 
     * @param <R> type of the query result
     */
    protected abstract class DaoQuery<R> implements Query<R, C> {

        @Override
        public R execute(C context) throws PersistenceException {
            return execute(createDaoInstance(), context);
        }

        /**
         * Executes the query.
         * 
         * @param dao data access object (DAO) to use
         * @param context data store context
         * @return the result of the query
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        protected abstract R execute(D dao, C context) throws PersistenceException;
    }
}
