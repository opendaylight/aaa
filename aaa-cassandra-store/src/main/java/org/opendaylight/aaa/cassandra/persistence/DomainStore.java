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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.base.Preconditions;
import org.opendaylight.aaa.api.IDMStoreUtil;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.Domains;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DomainStore extends AbstractStore<Domain,Domains>{
   public DomainStore(CassandraStore store) throws NoSuchMethodException {
      super(store,Domain.class,Domains.class,"setDomains","setDomainid");
   }
}
