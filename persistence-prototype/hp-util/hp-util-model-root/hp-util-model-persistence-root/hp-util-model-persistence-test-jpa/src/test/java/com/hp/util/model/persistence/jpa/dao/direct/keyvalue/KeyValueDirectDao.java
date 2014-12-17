/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.direct.keyvalue;

import com.hp.util.model.persistence.jpa.dao.JpaKeyValueDaoDirect;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class KeyValueDirectDao extends JpaKeyValueDaoDirect<Long, KeyValueDirectEntity> {

    /*
     * NOTE: An subclass of JpaKeyValueDaoDirect isn't actually needed. This class was created to
     * allow writing tests. Thus, the DAO subclass can be created in the test directory and for
     * production code an instance can be created like this:
     */
    /*
     * BaseDao<Long, KeyValueDirectEntity, JpaContext> dao = new JpaKeyValueDaoDirect<Long, KeyValueDirectEntity>(KeyValueDirectEntity.class);
     */
    /*
     * KeyValueDao<Long, KeyValueDirectEntity, JpaContext> dao = new JpaKeyValueDaoDirect<Long, KeyValueDirectEntity>(KeyValueDirectEntity.class);
     */
    /*
     * Integration test is still valuable to make sure entities are properly annotated. The test
     * will write entities to the database and then it will read them and assert the state.
     */

    public KeyValueDirectDao() {
        super(KeyValueDirectEntity.class);
    }
}
