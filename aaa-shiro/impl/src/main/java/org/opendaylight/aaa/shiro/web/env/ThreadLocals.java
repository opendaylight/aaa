/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;

/**
 * Holds ThreadLocal variables used to indirectly inject instances into classes that are instantiated by the Shiro
 * lib. Not ideal but a necessary evil to avoid static instances.
 *
 * @author Thomas Pantelis
 */
public interface ThreadLocals {
    ThreadLocal<DataBroker> DATABROKER_TL = new ThreadLocal<>();

    ThreadLocal<ICertificateManager> CERT_MANAGER_TL = new ThreadLocal<>();
}
