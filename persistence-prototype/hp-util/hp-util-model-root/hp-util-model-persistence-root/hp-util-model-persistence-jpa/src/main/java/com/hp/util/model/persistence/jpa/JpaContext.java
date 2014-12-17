/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa;

import javax.persistence.EntityManager;

/**
 * JPA query context.
 * 
 * @author Fabiel Zuniga
 */
public class JpaContext {

    private final EntityManager entityManager;

    /**
     * Creates a data store context.
     * 
     * @param entityManager entity manager
     */
    JpaContext(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Gets the entity manager.
     * 
     * @return the entity manager
     */
    public EntityManager getEntityManager() {
        return this.entityManager;
    }
}
