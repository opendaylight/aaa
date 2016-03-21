/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321;

import org.opendaylight.aaa.cert.impl.AaaCertMdsalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaaCertMdsalProviderModule extends AbstractAaaCertMdsalProviderModule {


    private final static Logger LOG = LoggerFactory.getLogger(AaaCertMdsalProviderModule.class);

    public AaaCertMdsalProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AaaCertMdsalProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.AaaCertMdsalProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        // TODO:implement
        LOG.info("The password is {} and intial vector is {}", this.getPassword(), this.getInitVector());
        return new AaaCertMdsalProvider();
    }

}
