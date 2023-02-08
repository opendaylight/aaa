/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import com.google.common.annotations.Beta;
import org.apache.shiro.web.env.WebEnvironment;

/**
 * Implementation-internal interface for bridging {@link AAAWebEnvironment} through OSGi Service Registry.
 */
@Beta
public interface AAAShiroWebEnvironment extends WebEnvironment {

}
