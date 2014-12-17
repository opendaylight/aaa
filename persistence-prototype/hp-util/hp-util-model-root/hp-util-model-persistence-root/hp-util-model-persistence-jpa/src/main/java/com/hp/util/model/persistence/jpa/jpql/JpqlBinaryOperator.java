/*
 * Copyright (c) 2012 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.jpa.jpql;

import javax.persistence.Query;

/**
 * Binary associative and commutative operator.
 * 
 * @author Fabiel Zuniga
 */
abstract class JpqlBinaryOperator implements JpqlPredicate {

    private String operator;
    private JpqlPredicate[] operands;

    /**
     * Creates a binary, associative and commutative operator.
     * 
     * @param operator operator
     * @param operands operands
     */
    protected JpqlBinaryOperator(String operator, JpqlPredicate... operands) {
        this.operator = operator;
        this.operands = operands;
    }

    @Override
    public String getPredicate() {
        if (this.operands == null || this.operands.length <= 0) {
            return "";
        }

        StringBuilder str = new StringBuilder(64);

        str.append('(');
        for (JpqlPredicate operand : this.operands) {
            str.append('(');
            str.append(operand.getPredicate());
            str.append(')');
            str.append(this.operator);
        }

        str.delete(str.length() - this.operator.length(), str.length());

        str.append(')');

        return str.toString();
    }

    @Override
    public void addParameters(Query query) {
        if (this.operands != null) {
            for (JpqlPredicate operand : this.operands) {
                operand.addParameters(query);
            }
        }
    }
}
