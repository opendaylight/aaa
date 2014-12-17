/*
 * Copyright (c) 2013 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.util.model.persistence.cassandra.client.astyanax;

import java.io.Serializable;

import com.hp.util.common.Decoder;
import com.hp.util.model.persistence.cassandra.column.Column;

/**
 * @author Fabiel Zuniga
 */
interface ColumnDecoder<C extends Serializable & Comparable<C>, D> extends
    Decoder<Column<C, D>, com.netflix.astyanax.model.Column<C>> {
}
