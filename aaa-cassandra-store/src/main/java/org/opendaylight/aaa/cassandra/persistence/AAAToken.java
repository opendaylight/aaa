/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cassandra.persistence;

/**
 *
 * @author saichler@gmail.com
 *
 */
@Deprecated
public class AAAToken {
    private String aaaToken = null;
    private Long experation = null;
    private String clientId = null;
    private String userId = null;
    private String username = null;
    private String domain = null;
    private String roles = null;

    public String getAaaToken() {
        return aaaToken;
    }

    public void setAaaToken(String aaaToken) {
        this.aaaToken = aaaToken;
    }

    public Long getExperation() {
        return experation;
    }

    public void setExperation(Long experation) {
        this.experation = experation;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
