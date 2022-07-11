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

    public void setUserid(final String id) {
        userid = id;
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
        return enabled == USER_ACCOUNT_ENABLED;
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

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setSalt(final String salt) {
        this.salt = salt;
    }

    public String getSalt() {
        return salt;
    }

    public String getDomainid() {
        return domainid;
    }

    public void setDomainid(final String domainid) {
        this.domainid = domainid;
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
        final User other = (User) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(getEmail(), other.getEmail())
            && isEnabled().equals(other.isEnabled()) && Objects.equals(getPassword(), other.getPassword())
            && Objects.equals(getSalt(), other.getSalt()) && Objects.equals(getUserid(), other.getUserid())
            && Objects.equals(getDescription(), other.getDescription());
    }

    @Override
    public String toString() {
        return name;
    }
}
