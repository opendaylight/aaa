/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.realm;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.aaa.api.TokenAuth;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * An empty {@link RealmAuthProvider}.
 */
@NonNullByDefault
@Component(factory = EmptyRealmAuthProvider.FACTORY_NAME)
public final class EmptyRealmAuthProvider implements RealmAuthProvider {
    /**
     * OSGi DS Component Factory name.
     */
    public static final String FACTORY_NAME = "org.opendaylight.aaa.shiro.realm.EmptyRealmAuthProvider";

    @Activate
    public EmptyRealmAuthProvider() {
        // Nothing else
    }

    @Override
    public List<TokenAuth> tokenAuthenticators() {
        return List.of();
    }
}
