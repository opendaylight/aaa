/*
 * Copyright (c) 2014, 2017 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.idm;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.opendaylight.aaa.api.ClaimCache;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.provider.GsonProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationBase;

/**
 * A JAX-RS application for IdmLight. The REST endpoints delivered by this application are in the form:
 * <code>http://{HOST}:{PORT}/auth/v1/</code>
 *
 * <p>
 * For example, the users REST endpoint is: <code>http://{HOST}:{PORT}/auth/v1/users</code>
 *
 * <p>
 * This application is responsible for interaction with the backing h2 database store.
 *
 * @see DomainHandler
 * @see UserHandler
 * @see RoleHandler
 * @author liemmn
 */
@ApplicationPath("/auth")
@JaxrsApplicationBase("/auth")
@Component(immediate = true, service = Application.class)
public class IdmLightApplication extends Application {
    // FIXME: create a bug to address the fact that the implementation assumes 128 as the max length, even though this
    //        claims 256.
    /**
     * The maximum field length for identity fields.
     */
    public static final int MAX_FIELD_LEN = 256;

    private final IIDMStore iidMStore;
    private final ClaimCache claimCache;

    @Activate
    @Inject
    public IdmLightApplication(@Reference final IIDMStore iidMStore, @Reference final ClaimCache claimCache) {
        this.iidMStore = requireNonNull(iidMStore);
        this.claimCache = requireNonNull(claimCache);
    }

    @Override
    public Set<Object> getSingletons() {
        return ImmutableSet.of(
            new GsonProvider<>(),
            new DomainHandler(iidMStore, claimCache),
            new RoleHandler(iidMStore, claimCache),
            new UserHandler(iidMStore, claimCache));
    }
}
