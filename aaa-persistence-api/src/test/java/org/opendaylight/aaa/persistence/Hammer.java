/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence;

public class Hammer {
    private Long id;
    private String manufacturer;
    private Integer weight;
    private Double tensile;
    public String getManufacturer() {
        return manufacturer;
    }
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    public Integer getWeight() {
        return weight;
    }
    public void setWeight(Integer weight) {
        this.weight = weight;
    }
    public Double getTensile() {
        return tensile;
    }
    public void setTensile(Double tensile) {
        this.tensile = tensile;
    }
    public Long getId() {
        return id;
    }
}
