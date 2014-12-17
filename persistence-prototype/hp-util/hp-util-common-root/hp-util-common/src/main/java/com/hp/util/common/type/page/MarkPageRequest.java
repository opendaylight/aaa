/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.common.type.page;

import com.hp.util.common.converter.ObjectToStringConverter;
import com.hp.util.common.type.Property;

/**
 * Mark based page request.
 * 
 * @param <M> type of the mark.
 * @author Fabiel Zuniga
 */
public class MarkPageRequest<M> extends PageRequest {
    private static final long serialVersionUID = 1L;

    private final M mark;
    private final Navigation navigation;

    /**
     * Creates a request for the first page.
     *
     * @param size page size or limit: Maximum number of data items per page
     */
    public MarkPageRequest(int size) {
        this(null, Navigation.NEXT, size);
    }

    /**
     * Creates a page request.
     *
     * @param mark mark or record to take as reference
     * @param navigation page navigation type
     * @param size page size or limit: Maximum number of data items per page
     * @throws NullPointerException if navigation is {@code null}
     * @throws IllegalArgumentException if {@code size} is less or equals than zero
     */
    public MarkPageRequest(M mark, Navigation navigation, int size) throws NullPointerException {
        super(size);
        if (navigation == null) {
            throw new NullPointerException("navigation cannot be null");
        }

        this.mark = mark;
        this.navigation = navigation;
    }

    /**
     * Gets the mark.
     *
     * @return the mark
     */
    public M getMark() {
        return this.mark;
    }

    /**
     * Gets the page navigation type.
     *
     * @return the page navigation type
     */
    public Navigation getNavigation() {
        return this.navigation;
    }

    /**
     * Converts the page request to use a converted mark.
     *
     * @param convertedMark converted mark
     * @return a mark page request with a converted mark
     */
    public <T> MarkPageRequest<T> convert(T convertedMark) {
        return new MarkPageRequest<T>(convertedMark, this.navigation, getSize());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getSize();
        result = prime * result + ((this.mark == null) ? 0 : this.mark.hashCode());
        result = prime * result + ((this.navigation == null) ? 0 : this.navigation.hashCode());
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

        MarkPageRequest<?> other = (MarkPageRequest<?>)obj;

        if (this.mark == null) {
            if (other.mark != null) {
                return false;
            }
        }
        else if (!this.mark.equals(other.mark)) {
            return false;
        }

        if (this.navigation != other.navigation) {
            return false;
        }

        if (getSize() != other.getSize()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("mark", this.mark),
                Property.valueOf("navigation", this.navigation),
                Property.valueOf("size", Integer.valueOf(getSize()))
        );
    }

    /**
     * Page navigation type.
     */
    public static enum Navigation {
        /**
         * Request the next page (Elements after the mark).
         */
        NEXT,
        /**
         * Requests the previous page (Elements previous to the mark).
         */
        PREVIOUS
    }
}
