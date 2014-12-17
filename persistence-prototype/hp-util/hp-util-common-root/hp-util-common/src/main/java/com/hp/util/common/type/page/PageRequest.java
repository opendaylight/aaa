/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.page;

import java.io.Serializable;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * Page request.
 * 
 * @author Fabiel Zuniga
 */
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int size;

    /**
     * Creates a page request.
     *
     * @param size page size or limit: Maximum number of data items per page
     * @throws IllegalArgumentException if {@code size} is less or equals than zero
     */
    public PageRequest(int size) throws IllegalArgumentException {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero");
        }
        this.size = size;
    }

    /**
     * Gets the page size or limit: Maximum number of data items per page.
     *
     * @return the page size
     */
    public int getSize() {
        return this.size;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.size;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        PageRequest other = (PageRequest)obj;

        if (this.size != other.size) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("size", Integer.valueOf(this.size))
        );
    }
}
