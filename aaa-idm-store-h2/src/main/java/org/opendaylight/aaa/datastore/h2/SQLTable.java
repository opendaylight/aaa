/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.datastore.h2;

import static com.google.common.base.Verify.verify;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of tables in our schema.
 */
@NonNullByDefault
enum SQLTable {
    /**
     * Domains.
     */
    // FIXME: Yeah, say more in documentation:
    //        - what is a domain?
    //        - how does it relate to others?
    DOMAIN(DomainStore.TABLE),
    /**
     * Users.
     */
    // FIXME: Yeah, say more in documentation
    USER(UserStore.TABLE),
    /**
     * Roles.
     */
    // FIXME: Yeah, say more in documentation
    ROLE(RoleStore.TABLE),
    /**
     * Grants. Probably just a (domain, user, role) tuple, but do not take my word for it.
     */
    // FIXME: Yeah, say more in documentation
    GRANT(GrantStore.TABLE);

    private final String tableName;

    SQLTable(final String name) {
        tableName = name;
    }

    void verifyTable(final String storeTableName) {
        verify(tableName.equals(storeTableName), "Mismatched table name '%s' with allocation %s", storeTableName, this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(name()).add("tableName", tableName).toString();
    }
}
