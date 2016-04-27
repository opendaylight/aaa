/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.rev160426;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.opendaylight.aaa.encrypt.AAAEncryptionService;
import org.opendaylight.aaa.encrypt.AAAEncryptionServiceImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *  @author - Sharon Aicler (saichler@gmail.com)
 */
public class AAAEncryptServiceModule extends org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.rev160426.AbstractAAAEncryptServiceModule {

    private BundleContext bundleContext = null;
    private static final Logger LOG = LoggerFactory.getLogger(AAAEncryptServiceModule.class);

    public AAAEncryptServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public AAAEncryptServiceModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.config.aaa.authn.encrypt.service.rev160426.AAAEncryptServiceModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        final AAAEncryptionServiceImpl impl = new AAAEncryptionServiceImpl(this);
        final ServiceRegistration<?> serviceRegistration = bundleContext.registerService(AAAEncryptionService.class.getName(), impl, null);

        LOG.info("AAA Enryption Service Loaded.");

        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                serviceRegistration.unregister();
            }
        };
    }

    public byte[] getEncryptionKeySalt(){
        StringTokenizer tokens = new StringTokenizer(getEncryptSalt(), ",");
        List<Byte> saltList = new ArrayList<>();
        while(tokens.hasMoreTokens()){
            String by = tokens.nextToken();
            saltList.add(Byte.parseByte(by.trim()));
        }
        byte salt[] = new byte[saltList.size()];
        int i=0;
        for(Byte b:saltList){
            salt[i] = b;
            i++;
        }
        return salt;
    }

    public void setBundleContext(BundleContext bundleContext){
        this.bundleContext = bundleContext;
    }
}
