/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.api;

import java.io.Serializable;

/**
 * A paging request.
 *
 * @author liemmn
 * @author Mark Mozolewski
 *
 * @param <ID> Identifier type of the objects in the page
 */
public interface Pageable<ID extends Serializable> {

    /**
     * Mark the start of a page with the given ID.
     *
     * @return ID to mark the start of a page
     */
    ID marker();

    /**
     * Return the maximum number of objects to return for each page.
     *
     * @return the maximum number of objects to return for each page
     */
    int limit();
}
