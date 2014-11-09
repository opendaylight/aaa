/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.impl.hibernate.search;

import org.opendaylight.aaa.persistence.api.Restrictable;
import org.opendaylight.aaa.persistence.api.Restriction;

import java.util.HashMap;
import java.util.Map;

public class SearchRestrictable implements Restrictable {

    Map<String, Restriction> restrictions = new HashMap<String, Restriction>();


    public SearchRestrictable(Map<String, Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public Map<String, Restriction> restrictions() {
        return restrictions;
    }


}
