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

@XmlRootElement(name = "user")
public class User {
    private static final int USER_ACCOUNT_ENABLED = 1;
    private static final int USER_ACCOUNT_DISABLED = 0;
    private static final int USER_ACCOUNT_DEFAULT_ENABLED = USER_ACCOUNT_ENABLED;

    private String userid;
    private String name;
    private String description;
    private int enabled = USER_ACCOUNT_DEFAULT_ENABLED;
    private String email;
    private String password;
    private String salt;
    private String domainid;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String id) {
        this.userid = id;
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
        return (enabled == USER_ACCOUNT_ENABLED);
    }

    public void setEnabled(final boolean enabled) {
        if (enabled) {
            setEnabled(USER_ACCOUNT_ENABLED);
        } else {
            setEnabled(USER_ACCOUNT_DISABLED);
        }
    }

    public void setEnabled(final int enabled) {
        this.enabled = enabled;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getSalt() {
        return this.salt;
    }

    public String getDomainid() {
        return domainid;
    }

    public void setDomainid(String domainid) {
        this.domainid = domainid;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        User other = (User) obj;
        if (other == null) {
            return false;
        }
        if (compareValues(getName(), other.getName())
                && compareValues(getEmail(), other.getEmail())
                && isEnabled().equals(other.isEnabled())
                && compareValues(getPassword(), other.getPassword())
                && compareValues(getSalt(), other.getSalt())
                && compareValues(getUserid(), other.getUserid())
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
