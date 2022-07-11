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

@XmlRootElement(name = "grant")
public class Grant {
    private String grantid;
    private String domainid;
    private String userid;
    private String roleid;

    public String getGrantid() {
        return grantid;
    }

    public void setGrantid(final String id) {
        grantid = id;
    }

    public String getDomainid() {
        return domainid;
    }

    public void setDomainid(final String id) {
        domainid = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(final String id) {
        userid = id;
    }

    public String getRoleid() {
        return roleid;
    }

    public void setRoleid(final String id) {
        roleid = id;
    }

    @Override
    public int hashCode() {
        return getUserid().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Grant other = (Grant) obj;
        return Objects.equals(getDomainid(), other.getDomainid()) && Objects.equals(getRoleid(), other.getRoleid())
            && Objects.equals(getUserid(), other.getUserid());
    }
}
