/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.impl.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.opendaylight.aaa.persistence.api.ObjectStore;
import org.opendaylight.aaa.persistence.api.Transportable;

import java.io.Serializable;


public class HibernatePersistence extends AbstractPersistenceService {
    private static SessionFactory factory;

    private static Logger log = Logger.getLogger(HibernatePersistence.class.getName());

    public HibernatePersistence() {
        init();
    }

    @Override
    public void init() {
        try{
            factory = new Configuration().configure().buildSessionFactory();
        }catch (Throwable e) {
            log.error("Failed to create sessionFactory object.", e);

            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public <T extends Transportable<ID>, ID extends Serializable> ObjectStore<T,ID> newObjectStore(Class<T> tClass) {
        return new HibernateObjectStore<T, ID>(tClass, factory);
    }
}
