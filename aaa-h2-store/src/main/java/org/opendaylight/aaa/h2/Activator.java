package org.opendaylight.aaa.h2;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.idm.persistence.H2Store;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
        manager.add(createComponent().setInterface(
                new String[] { IIDMStore.class.getName()}, null).setImplementation(new H2Store()));
    }

    @Override
    public void destroy(BundleContext context, DependencyManager manager)
            throws Exception {
    }
}

