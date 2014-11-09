/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.api;

import java.util.List;

/**
 * A criteria that determines the sorting order of the result set.
 * 
 * @author liemmn
 * @author Mark Mozolewski
 *
 */
public interface Orderable extends Criteria {
    /**
     * Return an ordered list of {@link Order}.
     * 
     * @return orders
     */
    List<Order> orders();
}
