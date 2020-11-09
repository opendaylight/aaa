/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.PasswordCredentialAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.cert.api.ICertificateManager;
import org.opendaylight.aaa.shiro.tokenauthrealm.auth.TokenAuthenticators;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.DatastoreConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;

@Component(immediate = true, service = TokenProvider.class)
public class OSGiAAAShiroProvider implements TokenProvider {

    private static final String MOON_ENDPOINT_PATH = "/moon";
    private static final String OATH_ENDPOINT_PATH = "/oath2";

    @Reference
    private DataBroker dataBroker;

    @Reference
    private ICertificateManager certificateManager;

    @Reference
    private PasswordCredentialAuth credentialAuth;

    @Reference
    private HttpService httpService;

    @Reference
    private ShiroConfiguration shiroConfiguration;

    @Reference
    private IIDMStore iidmStore;

    @Reference
    private AuthenticationService authenticationService;

    @Reference
    private PasswordHashService passwordHashService;

    private AAAShiroProvider shiroProvider = null;

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MANDATORY)
    void bindConfig(ShiroConfiguration shiroConfig, DatastoreConfig datastoreConfig) {
        updatedConfig(shiroConfig, datastoreConfig);
    }

    void unbindConfig(ShiroConfiguration shiroConfig, DatastoreConfig datastoreConfig) {
        shiroProvider.close();
        shiroProvider = null;
    }

    void updatedConfig(ShiroConfiguration shiroConfig, DatastoreConfig datastoreConfig) {
        shiroProvider = new AAAShiroProvider(dataBroker, certificateManager, credentialAuth, shiroConfig,
                httpService, MOON_ENDPOINT_PATH, OATH_ENDPOINT_PATH, datastoreConfig, iidmStore, authenticationService,
                passwordHashService);
        shiroProvider.init();
    }

    @Override
    public TokenAuthenticators getTokenAuthenticators() {
        return shiroProvider.getTokenAuthenticators();
    }

    @Override
    public TokenStore getTokenStore() {
        return shiroProvider.getTokenStore();
    }
}
