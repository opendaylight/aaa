/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.query;

import com.hp.util.common.Identifiable;
import com.hp.util.common.model.Dependent;
import com.hp.util.common.type.Id;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class DependentTestCase {

    public static class Owner implements Identifiable<Owner, Long> {

        @Override
        public <E extends Owner> Id<E, Long> getId() {
            return null;
        }
    }

    public static class DependentIdentifiable implements Identifiable<DependentIdentifiable, Long>,
            Dependent<Id<Owner, Long>> {

        @Override
        public Id<Owner, Long> getIndependent() {
            return null;
        }

        @Override
        public <E extends DependentIdentifiable> Id<E, Long> getId() {
            return null;
        }
    }
}
