/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.sts;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.ClaimAuth;
import org.opendaylight.aaa.api.ClientService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An activator for the secure token server to inject in a
 * {@link CredentialAuth} implementation.
 *
 * @author liemmn
 *
 */
public class Activator extends DependencyActivatorBase {
    public static final Logger logger = LoggerFactory.getLogger(Activator.class);
    private static boolean HARDWIRE_SERVICES=true;
    @Override
    public void init(BundleContext context, DependencyManager manager)
            throws Exception {
        manager.add(createComponent()
                .setImplementation(ServiceLocator.getInstance())
                .add(createServiceDependency().setService(CredentialAuth.class)
                    .setRequired(true)
                    .setCallbacks("credentialAuthAdded", "credentialAuthRemoved"))
                .add(createServiceDependency().setService(ClaimAuth.class)
                .setRequired(false)
                .setCallbacks("claimAuthAdded", "claimAuthRemoved"))
                .add(createServiceDependency().setService(TokenAuth.class)
                    .setRequired(false)
                        .setCallbacks("tokenAuthAdded", "tokenAuthRemoved"))
                .add(createServiceDependency().setService(TokenStore.class)
                        .setRequired(true)
                        .setCallbacks("tokenStoreAdded", "tokenStoreRemoved"))
                .add(createServiceDependency().setService(TokenStore.class)
                  .setRequired(true))
                .add(createServiceDependency().setService(
                        AuthenticationService.class).setRequired(true)
                        .setCallbacks("authenticationServiceAdded", "authenticationServiceRemoved"))
            .add(createServiceDependency().setService(IdMService.class)
                .setRequired(true))
            .add(createServiceDependency().setService(ClientService.class)
                        .setRequired(true)));

        new ServiceWireTask(AuthenticationService.class, context);
        new ServiceWireTask(IdMService.class, context);
        new ServiceWireTask(TokenAuth.class, context);
        new ServiceWireTask(TokenStore.class, context);
        new ServiceWireTask(ClientService.class, context);
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager)
            throws Exception {
    }

    private class ServiceWireTask extends Thread {
        private Class<?> serviceClass = null;
        private BundleContext context = null;

        public ServiceWireTask(Class<?> _serviceClass, BundleContext _context){
            this.serviceClass = _serviceClass;
            this.context = _context;
            this.start();
        }

        public void run(){
            ServiceReference<?> ref = null;
            while(ref==null){
                try{Thread.sleep(1000);}catch(Exception err){}
                ref = context.getServiceReference(this.serviceClass);
                if(ref==null){
                    logger.error("Service "+this.serviceClass.getSimpleName()+" is still missing");
                }
                if(ref!=null && HARDWIRE_SERVICES){
                    logger.info("Hardwirering Service "+this.serviceClass.getSimpleName());
                    Object service = context.getService(ref);
                    if(service instanceof IdMService){
                        ServiceLocator.getInstance().idmService = (IdMService)service;
                        ServiceLocator.getInstance().credentialAuth = (CredentialAuth)service;
                    }else
                    if(service instanceof AuthenticationService){
                        ServiceLocator.getInstance().authenticationService = (AuthenticationService)service;
                    }else
                    if(service instanceof TokenAuth){
                        ServiceLocator.getInstance().tokenAuthAdded((TokenAuth)service);
                    }else
                    if(service instanceof TokenStore){
                        ServiceLocator.getInstance().tokenStoreAdded((TokenStore)service);
                    }else
                    if(service instanceof ClientService){
                        ServiceLocator.getInstance().clientService = (ClientService)service;
                    }
                }
            }
        }
    }

}
