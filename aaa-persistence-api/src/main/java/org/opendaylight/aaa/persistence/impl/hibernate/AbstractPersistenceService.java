package org.opendaylight.aaa.persistence.impl.hibernate;


import org.apache.log4j.Logger;
import org.opendaylight.aaa.persistence.api.ObjectStore;
import org.opendaylight.aaa.persistence.api.Transportable;

import java.io.Serializable;

public abstract class AbstractPersistenceService implements PersistenceService {
    private static Logger log = Logger.getRootLogger();

    @Override
    public void init() {
    }

    @Override
    public <T extends Transportable<ID>, ID extends Serializable> ObjectStore newObjectStore(Class<T> tClass) {
        return null;
    }
}
