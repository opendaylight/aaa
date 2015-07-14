/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.idm;

import java.util.Hashtable;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.CredentialAuth;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.idm.ldap.LDAPConfiguration;
import org.opendaylight.aaa.idm.radius.RadiusConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

/**
 * An activator to publish the {@link CredentialAuth} implementation provided by
 * this bundle into OSGi.
 *
 * @author liemmn
 *
 */
public class Activator extends DependencyActivatorBase {

    public static final String LDAP_PID = "ldap";
    public static final String RADIUS_PID = "radius";

    @Override
    public void init(BundleContext context, DependencyManager manager)
            throws Exception {
        manager.add(createComponent().setInterface(
                new String[] { CredentialAuth.class.getName(),
                        IdMService.class.getName() }, null).setImplementation(
                IdmLightProxy.class));
        registerLDAPConfiguration(context, manager);
        registerRadiusConfiguration(context, manager);
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager)
            throws Exception {
    }

    private  void registerLDAPConfiguration(BundleContext context, DependencyManager manager){
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.SERVICE_PID, LDAP_PID);
        context.registerService(ManagedService.class.getName(), LDAPConfiguration.getInstance() , properties);
    }

    private void registerRadiusConfiguration(BundleContext context, DependencyManager manager){
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.SERVICE_PID, RADIUS_PID);
        context.registerService(ManagedService.class.getName(), RadiusConfiguration.getInstance() , properties);
    }    
}
