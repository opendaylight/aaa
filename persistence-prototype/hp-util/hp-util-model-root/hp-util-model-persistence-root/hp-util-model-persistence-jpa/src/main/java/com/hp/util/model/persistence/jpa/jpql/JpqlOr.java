/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.jpql;

/**
 * Or predicate.
 * 
 * @author Fabiel Zuniga
 */
class JpqlOr extends JpqlBinaryOperator {
    /**
     * Creates an AND predicate.
     * 
     * @param operands operands
     */
    public JpqlOr(JpqlPredicate... operands) {
        super(" Or ", operands);
    }
}
