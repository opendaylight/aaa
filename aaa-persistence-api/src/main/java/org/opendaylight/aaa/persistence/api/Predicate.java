/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.api;

/**
 * Predicate specification.
 * 
 * @author liemmn
 * @author Mark Mozolewski
 * 
 * @see Restriction
 *
 */
public enum Predicate {
    EQ, NEQ, LT, LTE, GT, GTE, IN, BETWEEN, REGEX
}
