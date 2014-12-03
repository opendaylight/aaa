/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.impl.hibernate.search;

import org.opendaylight.aaa.persistence.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchQueryable implements Queryable {

    Map<String, Restriction> restrictions = new HashMap<String, Restriction>();
    List<Order> orders = new ArrayList<Order>();


    public SearchQueryable(Map<String, Restriction> restrictions, List<Order> orders) {
        this.restrictions = restrictions;
        this.orders = orders;
    }

    @Override
    public Map<String, Restriction> restrictions() {
        return restrictions;
    }

    @Override
    public List<Order> orders() {
        return orders;
    }

}
