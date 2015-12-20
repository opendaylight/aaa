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

@XmlRootElement(name = "grant")
public class Grant {
    private String grantid;
    private String domainid;
    private String userid;
    private String roleid;

    public String getGrantid() {
        return this.grantid;
    }

    public void setGrantid(String id) {
        this.grantid = id;
    }

    public String getDomainid() {
        return domainid;
    }

    public void setDomainid(String id) {
        this.domainid = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String id) {
        this.userid = id;
    }

    public String getRoleid() {
        return roleid;
    }

    public void setRoleid(String id) {
        this.roleid = id;
    }

    @Override
    public int hashCode() {
        return this.getUserid().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Grant other = (Grant) obj;
        if (other == null)
            return false;
        if (compareValues(getDomainid(), other.getDomainid())
                && compareValues(getRoleid(), other.getRoleid())
                && compareValues(getUserid(), other.getUserid()))
            return true;
        return false;
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
