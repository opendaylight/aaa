/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api.model;

/**
 *
 * @author peter.mellquist@hp.com
 *
 */

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "domain")
public class Domain {
    private String domainid;
    private String name;
    private String description;
    private Boolean enabled;

    public String getDomainid() {
        return domainid;
    }

    public void setDomainid(String id) {
        this.domainid = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Domain other = (Domain) obj;
        if (other == null) {
            return false;
        }
        if (compareValues(getName(), other.getName()) && compareValues(getDomainid(), other.getDomainid())
                && compareValues(getDescription(), other.getDescription())) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }

    private boolean compareValues(Object c1, Object c2) {
        if (c1 == null && c2 != null) {
            return false;
        }
        if (c1 != null && c2 == null) {
            return false;
        }
        if (c1 == null && c2 == null) {
            return true;
        }
        if (c1.equals(c2)) {
            return true;
        }
        return false;
    }
}
