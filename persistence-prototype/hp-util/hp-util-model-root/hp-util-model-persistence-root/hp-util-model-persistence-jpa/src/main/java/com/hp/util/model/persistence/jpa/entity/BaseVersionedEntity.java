/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import com.hp.util.common.model.Versionable;

/**
 * Versioned entity that uses an auto-increment long type as primary key.
 * <p>
 * Note that entities must have a default constructor.
 * <P>
 * Notice that using generics in abstract entity classes which are resolved in concrete classes is
 * fine. Concrete entity classes must not use generics. Attributes types must be known at compile
 * time since the JPA provider generates some metadata classes representing the columns information.
 * 
 * @author Fabiel Zuniga
 */
@MappedSuperclass
public abstract class BaseVersionedEntity extends BaseEntity implements Versionable {

    @Version
    @Column(name = "Version")
    private Long version;

    @Override
    public Long getVersion() {
        return this.version;
    }
}
