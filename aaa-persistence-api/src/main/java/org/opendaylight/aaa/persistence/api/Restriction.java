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
 * A restriction specification.
 * 
 * @author liemmn
 * @author Mark Mozolewski
 *
 * @see Restrictable
 */
public class Restriction {
    private Predicate predicate;
    private Object value;
    
    public Predicate predicate() {
        return predicate;
    }
    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }
    public Object value() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
}
