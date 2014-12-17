/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.compositeid;

import java.io.Serializable;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class CompositeId implements Serializable {
    private static final long serialVersionUID = 1L;

    /*
     * NOTE: To use this class as key a default constructor must be provided which is a restriction
     * imposed by JPA. See com.hp.util.model.persistence.jpa.compositeid.preferred for a better
     * example to keep the key separated from JPA restrictions.
     */

    private String strKey;
    private int intKey;

    /**
     * This constructor is provided because of a restriction imposed by JPA and should not be used.
     */
    @Deprecated
    public CompositeId() {

    }

    public CompositeId(String strKey, int intKey) {
        if (strKey == null) {
            throw new NullPointerException("strKey cannot be null");
        }

        this.strKey = strKey;
        this.intKey = intKey;
    }

    public String getStrKey() {
        return this.strKey;
    }

    public int getIntKey() {
        return this.intKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.intKey;
        result = prime * result + this.strKey.hashCode();
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

        CompositeId other = (CompositeId)obj;

        if (this.intKey != other.intKey) {
            return false;
        }

        if (this.strKey == null) {
            if (other.strKey != null) {
                return false;
            }
        }
        else if (!this.strKey.equals(other.strKey)) {
            return false;
        }

        return true;
    }
}
