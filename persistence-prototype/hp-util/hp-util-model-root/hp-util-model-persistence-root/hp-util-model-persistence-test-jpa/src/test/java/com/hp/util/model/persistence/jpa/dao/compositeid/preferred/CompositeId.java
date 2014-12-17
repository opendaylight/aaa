/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.compositeid.preferred;

import java.io.Serializable;

import com.hp.util.common.type.Uid;

/**
 * @author Fabiel Zuniga
 */
@SuppressWarnings("javadoc")
public class CompositeId implements Serializable {
    private static final long serialVersionUID = 1L;

    private Uid uidKey;
    private int intKey;

    public CompositeId(Uid uidKey, int intKey) {
        if (uidKey == null) {
            throw new NullPointerException("uidKey cannot be null");
        }

        this.uidKey = uidKey;
        this.intKey = intKey;
    }

    public Uid getUidKey() {
        return this.uidKey;
    }

    public int getIntKey() {
        return this.intKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.intKey;
        result = prime * result + this.uidKey.hashCode();
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

        if (this.uidKey == null) {
            if (other.uidKey != null) {
                return false;
            }
        }
        else if (!this.uidKey.equals(other.uidKey)) {
            return false;
        }

        return true;
    }
}
