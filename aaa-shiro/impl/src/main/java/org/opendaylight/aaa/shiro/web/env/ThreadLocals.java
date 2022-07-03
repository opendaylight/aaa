/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.web.env;

import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.mdsal.binding.api.DataBroker;

/**
 * Holds ThreadLocal variables used to indirectly inject instances into classes that are instantiated by the Shiro
 * lib. Not ideal but a necessary evil to avoid static instances.
 *
 * @author Thomas Pantelis
 */
public final class ThreadLocals {
    public static final ThreadLocal<DataBroker> DATABROKER_TL = new ThreadLocal<>();

    public static final ThreadLocal<PasswordHashService> PASSWORD_HASH_SERVICE_TL = new ThreadLocal<>();

    private ThreadLocals() {
        // Hidden on purpose
    }
}
