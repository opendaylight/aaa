/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321;

import org.opendaylight.aaa.cert.impl.AaaCertMdsalProvider;

public class AaaCertMdsalProviderModule extends AbstractAaaCertMdsalProviderModule {

    public AaaCertMdsalProviderModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier, final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AaaCertMdsalProviderModule(final org.opendaylight.controller.config.api.ModuleIdentifier identifier, final org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, final org.opendaylight.yang.gen.v1.urn.opendaylight.yang.aaa.cert.mdsal.rev160321.AaaCertMdsalProviderModule oldModule, final java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final AaaCertMdsalProvider aaaCertMdsal = new AaaCertMdsalProvider();
        getAaaBrokerDependency().registerProvider(aaaCertMdsal);
        return aaaCertMdsal;
    }

    @Override
    public void customValidation() {

    }

}
