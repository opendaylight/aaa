/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.model;

/**
 * Grants.
 *
 * @author peter.mellquist@hp.com
 */
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "grants")
public class Grants {
    private List<Grant> grants = new ArrayList<>();

    public void setGrants(List<Grant> grants) {
        this.grants = grants;
    }

    public List<Grant> getGrants() {
        return grants;
    }

    @Override
    public String toString() {
        return "Grants" + grants.toString();
    }

}
