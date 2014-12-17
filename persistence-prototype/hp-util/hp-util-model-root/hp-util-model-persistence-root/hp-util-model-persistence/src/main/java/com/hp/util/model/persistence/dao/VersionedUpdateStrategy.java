/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.dao;

import com.hp.util.common.Identifiable;
import com.hp.util.common.Util;
import com.hp.util.common.model.Versionable;

/**
 * When a versioned object is updated on the database its version is checked; if the stored object
 * and object to store versions don't match the operation fails.
 * 
 * @param <P> type of the object directly written or read from the data store (an object that can be
 *            directly used by the underlying data store or database technology)
 * @param <T> type of the identifiable object (object to store in the data store)
 * @author Fabiel Zuniga
 */
public class VersionedUpdateStrategy<P extends Versionable, T extends Identifiable<? super T, ?> & Versionable>
    implements UpdateStrategy<P, T> {

    @Override
    public void validateRead(P source, T target) {
        // The following line must be performed by the concrete DAO.
        // target.setVersion(source.getVersion());
        checkVersion(source, target);
    }

    @Override
    public void validateWrite(P target, T source) {
        checkVersion(source, target);
    }

    private static void checkVersion(Versionable source, Versionable target) throws IllegalStateException {
        if (!Util.equals(source.getVersion(), target.getVersion())) {
            throw new IllegalStateException("Incompatible versions [source: " + source.getVersion() + ", target: "
                    + target.getVersion() + "]");
        }
    }
}
