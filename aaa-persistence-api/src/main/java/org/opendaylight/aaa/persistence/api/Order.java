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
 * An ordering specification.
 *
 * @author liemmn
 * @author Mark Mozolewski
 *
 * @see Orderable
 */
public class Order {
    public static enum Direction {
        ASC, DESC
    }

    private Direction direction;
    private String attributeName;
    
    public Order() {}

    /**
     * @param attributeName name of attribute
     * @param direction direction to sort
     */
    public Order(final String attributeName, final Direction direction) {
        this.direction = direction;
        this.attributeName = attributeName;
    }

    public Direction direction() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String attributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

}
