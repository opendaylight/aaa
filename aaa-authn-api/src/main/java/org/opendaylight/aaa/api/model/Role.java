/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
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

@XmlRootElement(name = "role")
public class Role {
    private String roleid;
    private String name;
    private String description;
    private String domainid;

    public String getRoleid() {
        return roleid;
    }

    public void setRoleid(String id) {
        this.roleid = id;
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

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Role other = (Role) obj;
        if (other == null)
            return false;
        if (compareValues(getName(), other.getName())
                && compareValues(getRoleid(), other.getRoleid())
                && compareValues(getDescription(), other.getDescription()))
            return true;
        return false;
    }

    public void setDomainid(String domainid) {
        this.domainid = domainid;
    }

    public String getDomainid() {
        return this.domainid;
    }

    private boolean compareValues(Object a, Object b) {
        if (a == null && b != null)
            return false;
        if (a != null && b == null)
            return false;
        if (a == null && b == null)
            return true;
        if (a.equals(b))
            return true;
        return false;
    }
}
