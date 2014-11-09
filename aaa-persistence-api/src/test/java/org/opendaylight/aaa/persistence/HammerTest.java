/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opendaylight.aaa.persistence.api.Order;
import org.opendaylight.aaa.persistence.api.Predicate;
import org.opendaylight.aaa.persistence.api.Queryable;
import org.opendaylight.aaa.persistence.api.Restriction;
import org.opendaylight.aaa.persistence.api.Order.Direction;

public class HammerTest {
    
    @Test
    public void testStore() {
        HammerStore store = new HammerStore();
        Hammer h1 = new Hammer();
        SledgeHammer h2 = new SledgeHammer();
        store.save(Arrays.asList(h1, h2));
    }

    @Test
    public void testQBE() {
        HammerStore store = new HammerStore();
        final Hammer hammer = new Hammer();
        hammer.setTensile(240.0);
        hammer.setManufacturer("\\b(Stanley|Craftsman)\\b");
        // Note:  Queryable needs a builder!
        store.findAll(hammer, new Queryable() {
            
            @Override
            public Map<String, Restriction> restrictions() {
                Map<String, Restriction> restrictions = new HashMap<>();
                // Weight range
                Restriction weight = new Restriction();
                weight.setPredicate(Predicate.BETWEEN);
                weight.setValue(Arrays.asList(1, 5));
                restrictions.put("weight", weight);
                // No wimpy hammer (at least 240 tensile strength)
                Restriction tensile = new Restriction();
                tensile.setPredicate(Predicate.GTE);
                restrictions.put("tensile", tensile);
                // Any Stanley or Craftsman hammer will do
                Restriction manu = new Restriction();
                manu.setPredicate(Predicate.REGEX);
                restrictions.put("manufacturer", manu);
                return Collections.unmodifiableMap(restrictions);
            }
            
            @Override
            public List<Order> orders() {
                List<Order> orders = new ArrayList<>();
                orders.add(new Order("tensile", Direction.ASC));
                return orders;
            }
        });
    }
}
