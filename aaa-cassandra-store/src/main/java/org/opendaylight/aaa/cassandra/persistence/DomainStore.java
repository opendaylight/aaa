/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cassandra.persistence;

/**
 *
 * @author peter.mellquist@hp.com
 *
 */
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;

@Deprecated
public class DomainStore extends AbstractStore<Domain,Domains>{
    public DomainStore(CassandraStore store) throws NoSuchMethodException {
       super(store,Domain.class,Domains.class,"setDomains","setDomainid");
    }
}
