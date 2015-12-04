/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.idm;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.AuthenticationService;
import org.opendaylight.aaa.api.ClientService;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.TokenAuth;
import org.opendaylight.aaa.api.TokenStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An activator to publish the {@link CredentialAuth} implementation provided by
 * this bundle into OSGi.
 *
 * @author liemmn
 *
 */
public class Activator extends DependencyActivatorBase {
    public static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);
    public static final int WIRE_SERVICE_RETRY_COUNT = 60;
    public static final int RETRY_CHECK_INTERVAL = 2000;

    @Override
    public void init(BundleContext context, DependencyManager manager)
            throws Exception {
        manager.add(createComponent().setInterface(
                new String[] { CredentialAuth.class.getName(),
                        IdMService.class.getName() }, null).setImplementation(
                IdmLightProxy.class));
        manager.add(createComponent()
                .setImplementation(ServiceLocator.INSTANCE)
                .add(createServiceDependency().setService(IIDMStore.class).setRequired(true)));

        ServiceWireTask wireTask = new ServiceWireTask(context);
        wireTask.addServiceMonitor(IIDMStore.class);
        wireTask.start();
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager)
            throws Exception {
    }

    private class ServiceWriteTaskEntry {
        private final  Class<?> serviceClass;
        private volatile int retryCount = 0;
        public ServiceWriteTaskEntry(final Class<?> serviceClass){
            this.serviceClass = serviceClass;
        }
        public boolean shouldRetry(){
            retryCount++;
            if(retryCount>=WIRE_SERVICE_RETRY_COUNT)
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

        public ServiceWireTask(final BundleContext _context){
            this.context = _context;
        }

        public final void addServiceMonitor(final Class<?> serviceClass){
            ServiceWriteTaskEntry entry = new ServiceWriteTaskEntry(serviceClass);
            this.entries.add(entry);
        }

        public void start(){
            new Thread(this,"IDM Light Service Wiring Task").start();
        }

        public void run(){
            ServiceReference<?> ref = null;
            while(!this.entries.isEmpty()){
                try {
                    //Wait 2 seconds between retries, each entry has 10 tries before we give up
                    Thread.sleep(RETRY_CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted",e);
                    //kill the thread.
                    return;
                }
                //Using iterator so i could remove an entry while iterating
                for(Iterator<ServiceWriteTaskEntry> iter = this.entries.iterator(); iter.hasNext();){
                    ServiceWriteTaskEntry entry = iter.next();
                    if(entry.shouldRetry()) {
                        ref = context.getServiceReference(entry.getServiceClass());
                        if(ref==null){
                            LOGGER.info("Could not locate service of type {}, attempt {} out of {}. ",entry.getServiceClass().getSimpleName(),entry.getRetryCount(),WIRE_SERVICE_RETRY_COUNT);
                        }else {
                            LOGGER.info("Wiring Service {}",entry.getServiceClass().getSimpleName());
                            Object service = context.getService(ref);
                            if(service instanceof IIDMStore){
                                if(ServiceLocator.getInstance().getStore()==null) {
                                    ServiceLocator.getInstance().setStore((IIDMStore) service);
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
            LOGGER.info("IDM Light Wiring Service finished.");
        }
    }
}
