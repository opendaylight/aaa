/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import java.util.Hashtable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.blueprint.container.BlueprintListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bundle Activator.
 *
 * @author Thomas Pantelis
 */
public class AAAShiroActivator implements BundleActivator, BlueprintListener {
    private static final Logger LOG = LoggerFactory.getLogger(AAAShiroActivator.class);

    private ServiceRegistration<?> eventHandlerReg;
    private Bundle thisBundle;

    @Override
    public void start(BundleContext context) {
        thisBundle = context.getBundle();
        eventHandlerReg = context.registerService(BlueprintListener.class.getName(), this, new Hashtable<>());
    }

    @Override
    public void stop(BundleContext context) {
        AAAShiroProvider.getInstanceFuture().completeExceptionally(
                new RuntimeException(String.format("%s bundle stopped", thisBundle)));

        if (eventHandlerReg != null) {
            try {
                eventHandlerReg.unregister();
            } catch (IllegalStateException e) {
                // This can be safely ignored
            }
        }
    }

    @Override
    public void blueprintEvent(BlueprintEvent event) {
        if (!thisBundle.equals(event.getBundle())) {
            return;
        }

        LOG.info("Blueprint container event for bundle {}: {}", thisBundle, event.getType());

        if (event.getType() == BlueprintEvent.DESTROYING) {
            AAAShiroProvider.getInstanceFuture().completeExceptionally(new RuntimeException(
                    String.format("Blueprint container for bundle %s is being destroyed", thisBundle)));
        }
    }
}
