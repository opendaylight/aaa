/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.model;

import java.util.Objects;
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

    public void setDomainid(final String id) {
        domainid = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Domain other = (Domain) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(getDomainid(), other.getDomainid())
            && Objects.equals(getDescription(), other.getDescription());
    }

    @Override
    public String toString() {
        return name;
    }
}
