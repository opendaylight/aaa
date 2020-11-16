/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.shiro.idm;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.util.List;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.ClaimCache;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Beta
@Component(immediate = true, property = "type=default",
           service = { CredentialAuth.class, PasswordCredentialAuth.class, IdMService.class, ClaimCache.class })
public class OSGIIdmLightProxy extends ForwardingObject implements PasswordCredentialAuth, IdMService, ClaimCache {

    @Reference
    IIDMStore iidmStore;

    @Reference
    PasswordHashService passwordHashService;

    private IdmLightProxy delegate;

    @Activate
    void activate() {
        delegate = new IdmLightProxy(iidmStore, passwordHashService);
    }

    @Override
    protected IdmLightProxy delegate() {
        return delegate;
    }

    @Override
    public void clear() {
        delegate().clear();
    }

    @Override
    public List<String> listDomains(String userId) {
        return delegate().listDomains(userId);
    }

    @Override
    public List<String> listRoles(String userId, String domainName) {
        return delegate().listRoles(userId, domainName);
    }

    @Override
    public List<String> listUserIDs() throws IDMStoreException {
        return delegate().listUserIDs();
    }

    @Override
    public Claim authenticate(PasswordCredentials cred) throws AuthenticationException {
        return delegate().authenticate(cred);
    }
}
