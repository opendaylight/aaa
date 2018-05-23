/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.shiro.idm;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.opendaylight.aaa.api.ClaimCache;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.provider.GsonProvider;

/**
 * A JAX-RS application for IdmLight. The REST endpoints delivered by this
 * application are in the form: <code>http://{HOST}:{PORT}/auth/v1/</code>
 *
 * <p>
 * For example, the users REST endpoint is:
 * <code>http://{HOST}:{PORT}/auth/v1/users</code>
 *
 * <p>
 * This application is responsible for interaction with the backing h2 database
 * store.
 *
 * @author liemmn
 *
 * @see org.opendaylight.aaa.shiro.idm.DomainHandler
 * @see org.opendaylight.aaa.shiro.idm.UserHandler
 * @see org.opendaylight.aaa.shiro.idm.RoleHandler
 */
public class IdmLightApplication extends Application {

    // TODO create a bug to address the fact that the implementation assumes 128
    // as the max length, even though this claims 256.
    /**
     * The maximum field length for identity fields.
     */
    public static final int MAX_FIELD_LEN = 256;

    private final IIDMStore iidMStore;
    private final ClaimCache claimCache;

    public IdmLightApplication(IIDMStore iidMStore, ClaimCache claimCache) {
        this.iidMStore = Objects.requireNonNull(iidMStore);
        this.claimCache = Objects.requireNonNull(claimCache);
    }

    @Override
    public Set<Object> getSingletons() {
        return ImmutableSet.builderWithExpectedSize(4)
                    .add(new GsonProvider<>())
                    .add(new DomainHandler(iidMStore, claimCache))
                    .add(new RoleHandler(iidMStore, claimCache))
                    .add(new UserHandler(iidMStore, claimCache))
                    .build();
    }
}
