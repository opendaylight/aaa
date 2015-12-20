/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.api.model;

/**
 *
 * @author peter.mellquist@hp.com
 *
 */

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "domains")
public class Domains {
    private List<Domain> domains = new ArrayList<Domain>();

    public void setDomains(List<Domain> domains) {
        this.domains = domains;
    }

    public List<Domain> getDomains() {
        return domains;
    }

}
