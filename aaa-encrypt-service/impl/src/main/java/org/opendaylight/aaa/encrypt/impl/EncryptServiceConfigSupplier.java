/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.encrypt.impl;

import java.util.function.Supplier;
import org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.config.rev160915.EncryptServiceConfig;

/**
 * Blueprint compatibility hack. Direct injection would result in
 * <pre>
 *   Caused by: java.lang.IncompatibleClassChangeError: class Proxy601f54db_5975_4863_a33b_758ee248518f cannot implement
 *              sealed interface org.opendaylight.yangtools.yang.binding.DataContainer
 * </pre>
 * and hence we wrap it with a DataObject, which is not sealed.
 */
@Deprecated
public interface EncryptServiceConfigSupplier extends Supplier<EncryptServiceConfig> {

}
