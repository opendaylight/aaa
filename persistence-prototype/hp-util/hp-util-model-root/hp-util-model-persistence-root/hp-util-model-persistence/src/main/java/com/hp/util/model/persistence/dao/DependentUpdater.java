/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.hp.util.common.Identifiable;
import com.hp.util.common.Provider;
import com.hp.util.common.type.tuple.Pair;
import com.hp.util.model.persistence.PersistenceException;

/**
 * Conforms a collection of dependents in a one-to-many relation.
 * 
 * @author Fabiel Zuniga
 */
public final class DependentUpdater {

    private DependentUpdater() {

    }

    /**
     * Updates a collection of dependent in a one-to-many relation.
     * 
     * @param <I> type of the identifiable object's id. This type should be immutable and it is
     *            critical it implements {@link Object#equals(Object)} and {@link Object#hashCode()}
     *            correctly.
     * @param <P> type of the object directly written or read from the data store (an object that
     *            can be directly used by the underlying data store or database technology)
     * @param <T> type of the identifiable object (object to store in the data store)
     * @param currentDependents currently persisted as dependents
     * @param newDependents new dependent entities
     * @param idProvider id provider for the currently persisted dependents
     * @param delegate dependent updater
     * @throws PersistenceException if persistence errors occur while executing the operation
     */
    public static <I extends Serializable, P, T extends Identifiable<? super T, I>> void updateDependents(
            Collection<P> currentDependents, Collection<T> newDependents, Provider<I, P> idProvider,
            Delegate<P, T> delegate)
            throws PersistenceException {
        Collection<T> dependentsToAdd = new ArrayList<T>(newDependents.size());
        Collection<Pair<P, T>> dependentsToUpdate = new ArrayList<Pair<P, T>>(newDependents.size());
        Collection<P> dependentsToDelete = new ArrayList<P>(newDependents.size());

        for (T newDependent : newDependents) {
            if (newDependent.getId() == null) {
                // Auto-generated keys case
                dependentsToAdd.add(newDependent);
            }
            else {
                boolean add = true;
                for (P currentDependent : currentDependents) {
                    if (newDependent.getId().getValue().equals(idProvider.get(currentDependent))) {
                        dependentsToUpdate.add(Pair.valueOf(currentDependent, newDependent));
                        add = false;
                        break;
                    }
                }
                if (add) {
                    dependentsToAdd.add(newDependent);
                }
            }
        }

        for (P currentDependent : currentDependents) {
            boolean delete = true;
            for (T newDependent : newDependents) {
                if (newDependent.getId() != null
                        && newDependent.getId().getValue().equals(idProvider.get(currentDependent))) {
                    delete = false;
                    break;
                }
            }
            if (delete) {
                dependentsToDelete.add(currentDependent);
            }
        }

        for (P dependentEntity : dependentsToDelete) {
            delegate.delete(dependentEntity);
        }

        for (Pair<P, T> tuple : dependentsToUpdate) {
            delegate.update(tuple.getFirst(), tuple.getSecond());
        }

        for (T dependent : dependentsToAdd) {
            delegate.add(dependent);
        }
    }

    /**
     * Delegate to conform a collection of dependents.
     * 
     * @param <P> type of the object as in the data store
     * @param <T> type of the object returned or retrieved from the client
     */
    public static interface Delegate<P, T extends Identifiable<?, ?>> {

        /**
         * Adds a new dependent.
         * 
         * @param dependent the dependent to add
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public void add(T dependent) throws PersistenceException;

        /**
         * Updates a dependent.
         * 
         * @param targetDependent object to update
         * @param sourceDependent object to take the data from
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public void update(P targetDependent, T sourceDependent) throws PersistenceException;

        /**
         * Deletes a dependent.
         * 
         * @param dependent dependent to delete
         * @throws PersistenceException if persistence errors occur while executing the operation
         */
        public void delete(P dependent) throws PersistenceException;
    }
}
