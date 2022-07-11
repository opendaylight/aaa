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

@XmlRootElement(name = "role")
public class Role {
    private String roleid;
    private String name;
    private String description;
    private String domainid;

    public String getRoleid() {
        return roleid;
    }

    public void setRoleid(final String id) {
        roleid = id;
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
        final Role other = (Role) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(getRoleid(), other.getRoleid())
            && Objects.equals(getDescription(), other.getDescription());
    }

    @Override
    public String toString() {
        return name;
    }

    public void setDomainid(final String domainid) {
        this.domainid = domainid;
    }

    public String getDomainid() {
        return domainid;
    }
}
