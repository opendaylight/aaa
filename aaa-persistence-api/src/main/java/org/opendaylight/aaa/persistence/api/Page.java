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
import java.util.Iterator;

/**
 * A paging result of the given object type.
 *
 * @author liemmn
 * @author Mark Mozolewski
 *
 * @param <T> Object type in the given page
 * @param <ID> Identifier type of objects in the given page
 */
public interface Page<T, ID extends Serializable> {
    /**
     * Check to see if this page has any data.
     *
     * @return true if empty, false otherwise
     */
    boolean isEmpty();

    /**
     * Return the content of the page.
     *
     * @return content of the page
     */
    Iterator<T> content();

    /**
     * Check to see if there is more data after this page.
     *
     * @return true if more data, false otherwise
     */
    boolean hasNext();

    /**
     * Get the marker for the next page.
     *
     * @return marker for the next page, null if none
     */
    ID nextMarker();

    /**
     * Check to see if there is more data before this page.
     *
     * @return true if more data, false otherwise
     */
    boolean hasPrevious();

    /**
     * Get the marker for the previous page.
     *
     * @return marker for the previous page, null if none
     */
    ID previousMarker();
}
