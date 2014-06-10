/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import static org.opendaylight.aaa.EqualUtil.areEqual;
import static org.opendaylight.aaa.HashCodeUtil.hash;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.opendaylight.aaa.api.Claim;

/**
 * Builder for a {@link Claim}
 *
 * @author liemmn
 *
 */
public class ClaimBuilder implements Claim {

    protected int hashCode = 0;

    protected String userId;
    protected String userName;
    protected String tenantId;
    protected String tenantName;
    protected final Set<String> roles = new HashSet<String>();

    public ClaimBuilder() {}

    public ClaimBuilder(Claim claim) {
        userId = claim.userId();
        userName = claim.userName();
        tenantId = claim.tenantId();
        tenantName = claim.tenantName();
        roles.addAll(claim.roles());
    }

    @Override
    public String userId() {
        return userId;
    }

    public ClaimBuilder setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public String userName() {
        return userName;
    }

    public ClaimBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public String tenantId() {
        return tenantId;
    }

    public ClaimBuilder setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String tenantName() {
        return tenantName;
    }

    public ClaimBuilder setTenantName(String tenantName) {
        this.tenantName = tenantName;
        return this;
    }

    @Override
    public Set<String> roles() {
        return Collections.unmodifiableSet(roles);
    }

    public ClaimBuilder addRoles(Set<String> roles) {
        this.roles.addAll(roles);
        return this;
    }

    public ClaimBuilder addRole(String role) {
        this.roles.add(role);
        return this;
    }

    public Claim build() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Claim))
            return false;
        Claim a = (Claim) o;
        return areEqual(roles, a.roles())
                && areEqual(tenantId, a.tenantId())
                && areEqual(tenantName, a.tenantName())
                && areEqual(userId, a.userId())
                && areEqual(userName, a.userName());
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int result = HashCodeUtil.SEED;
            result = hash(result, userId);
            result = hash(result, userName);
            result = hash(result, tenantId);
            result = hash(result, tenantName);
            result = hash(result, roles);
            hashCode = result;
        }
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (userId != null) sb.append("userId:").append(userId).append(",");
        if (userName != null) sb.append("userName:").append(userName).append(",");
        if (tenantId != null) sb.append("tenantId:").append(tenantId).append(",");
        if (tenantName != null) sb.append("tenantName:").append(tenantName).append(",");
        sb.append("roles:").append(roles);
        return sb.toString();
    }

}
