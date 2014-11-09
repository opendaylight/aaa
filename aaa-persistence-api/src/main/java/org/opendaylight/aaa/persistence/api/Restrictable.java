/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.persistence.api;

import java.util.Map;

/**
 * A criteria that restricts the result set.
 * 
 * @author liemmn
 * @author Mark Mozolewski
 *
 */
public interface Restrictable extends Criteria {
    /**
     * Returns a map of attribute names to restrictions placed on the attribute
     * values.
     * 
     * @return map of attribute restrictions
     */
    Map<String, Restriction> restrictions();
}
