/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.dao.versioned;

import java.util.Date;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.hp.util.model.persistence.jpa.dao.mock.EnumMock;

/**
 * Manually created meta-model for unit test.
 * <P>
 * Meta model classes are usually (and should be) auto-generated at compile time by the JPA
 * implementation.
 * <P>
 * [JPA 2 Specification, section 6.2.2, pg 200] Values are assigned by provider when
 * EntityManagerFactory is created. Attributes must be volatile to guarantee that other threads can
 * see values assigned by provider.
 * 
 * @author Fabiel Zuniga
 */
@StaticMetamodel(VersionedEntity.class)
public abstract class VersionedEntity_ {
    /** */
    public static volatile SingularAttribute<VersionedEntity, Long> id;
    /** */
    public static volatile SingularAttribute<VersionedEntity, Long> version;
    /** */
    public static volatile SingularAttribute<VersionedEntity, String> attributeString;
    /** */
    public static volatile SingularAttribute<VersionedEntity, Boolean> attributeBoolean;
    /** */
    public static volatile SingularAttribute<VersionedEntity, Long> attributeLong;
    /** */
    public static volatile SingularAttribute<VersionedEntity, Date> attributeDate;
    /** */
    public static volatile SingularAttribute<VersionedEntity, EnumMock> attributeEnum;
}
