/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.impl.hibernate;

import org.opendaylight.aaa.persistence.api.ObjectStore;
import org.opendaylight.aaa.persistence.api.Transportable;

import java.io.Serializable;

public interface PersistenceService {
    void init();
    <T extends Transportable<ID>, ID extends Serializable> ObjectStore newObjectStore(Class<T> tClass);
}
