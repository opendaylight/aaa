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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An activator for the secure token server to inject in a
 * {@link CredentialAuth} implementation.
 *
 * @author liemmn
 *
 */
public class Activator extends DependencyActivatorBase {
    public static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);
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

        ServiceWireTask wireTask = new ServiceWireTask(context);
        wireTask.addServiceMonitor(AuthenticationService.class);
        wireTask.addServiceMonitor(IdMService.class);
        wireTask.addServiceMonitor(TokenAuth.class);
        wireTask.addServiceMonitor(TokenStore.class);
        wireTask.addServiceMonitor(ClientService.class);
        wireTask.start();
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager)
            throws Exception {
    }

    private class ServiceWriteTaskEntry {
        private final Class<?> serviceClass;
        private int retryCount = 0;
        public ServiceWriteTaskEntry(Class<?> serviceClass){
            this.serviceClass = serviceClass;
        }
        public boolean shouldRetry(){
            retryCount++;
            if(retryCount>=10)
                return false;
            return true;
        }

        public int getRetryCount(){
            return this.retryCount;
        }

        public Class<?> getServiceClass(){
            return this.serviceClass;
        }
    }

    private class ServiceWireTask implements Runnable {
        private final BundleContext context;
        private final List<ServiceWriteTaskEntry> entries = new LinkedList<ServiceWriteTaskEntry>();

        public ServiceWireTask(BundleContext _context){
            this.context = _context;
        }

        public void addServiceMonitor(Class<?> serviceClass){
            ServiceWriteTaskEntry entry = new ServiceWriteTaskEntry(serviceClass);
            this.entries.add(entry);
        }

        public void start(){
            new Thread(this,"STS Service Wiring Task").start();
        }

        public void run(){
            ServiceReference<?> ref = null;
            while(!this.entries.isEmpty()){
                try {
                    //Wait 2 seconds between retries, each entry has 10 tries before we give up
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted",e);
                    //kill the thread.
                    return;
                }
                //Using iterator so i could remove an entry while iterating
                for(Iterator<ServiceWriteTaskEntry> iter=this.entries.iterator();iter.hasNext();){
                    ServiceWriteTaskEntry entry = iter.next();
                    if(entry.shouldRetry()) {
                        ref = context.getServiceReference(entry.getServiceClass());
                        if(ref==null){
                            LOGGER.info("Could not locate service of type {}, attempt {} out of 10. ",entry.getServiceClass().getSimpleName(),entry.getRetryCount());
                        }else {
                            LOGGER.info("Wiring Service "+entry.getServiceClass());
                            Object service = context.getService(ref);
                            if(service instanceof IdMService){
                                if(ServiceLocator.getInstance().getIdmService()==null) {
                                    ServiceLocator.getInstance().setIdmService((IdMService) service);
                                }
                                if(ServiceLocator.getInstance().getCredentialAuth()==null) {
                                    ServiceLocator.getInstance().setCredentialAuth((CredentialAuth)service);
                                }
                            }else
                            if(service instanceof AuthenticationService){
                                if(ServiceLocator.getInstance().getAuthenticationService()==null) {
                                    ServiceLocator.getInstance().setAuthenticationService((AuthenticationService) service);
                                }
                            }else
                            if(service instanceof TokenAuth){
                                if(ServiceLocator.getInstance().getTokenAuthCollection().isEmpty()) {
                                    ServiceLocator.getInstance().tokenAuthAdded((TokenAuth) service);
                                }
                            }else
                            if(service instanceof TokenStore){
                                if(ServiceLocator.getInstance().getTokenStore()==null) {
                                    ServiceLocator.getInstance().setTokenStore((TokenStore) service);
                                }
                            }else
                            if(service instanceof ClientService){
                                if(ServiceLocator.getInstance().getClientService()==null) {
                                    ServiceLocator.getInstance().setClientService((ClientService) service);
                                }
                            }
                            //we found the service, the entry can be removed.
                            iter.remove();
                        }

                    }else{
                        LOGGER.error("Could not locate service of type {}, giving up after 10 tries. ",entry.getServiceClass().getSimpleName());
                        //We are giving up on this service
                        iter.remove();
                    }
                }
            }
            LOGGER.info("STS Wiring Service finished.");
        }
    }

}
