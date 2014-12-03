/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.impl.hibernate.search;

import org.opendaylight.aaa.persistence.api.Pageable;
import org.opendaylight.aaa.persistence.api.Transportable;

import java.io.Serializable;

public class SearchPagable<T extends Transportable<ID>, ID extends Serializable> implements Pageable<ID> {
    ID marker;
    int limit;

    @Override
    public ID marker() {
        return this.marker;
    }

    @Override
    public int limit() {
        return this.limit;
    }

    public void setMarker(ID id) {
        this.marker = id;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
