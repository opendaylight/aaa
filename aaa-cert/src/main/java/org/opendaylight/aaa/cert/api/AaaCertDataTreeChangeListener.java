/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cert.api;

import java.util.Collection;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yangtools.yang.binding.DataObject;

public abstract class AaaCertDataTreeChangeListener <D extends DataObject> implements DataTreeChangeListener<D>, AutoCloseable {

    protected DataBroker dataBroker;

    public AaaCertDataTreeChangeListener(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<D>> collection) {
        for (final DataTreeModification<D> change : collection) {
            final DataObjectModification<D> root = change.getRootNode();
            switch (root.getModificationType()) {
                case SUBTREE_MODIFIED:
                    update(change);
                    break;
                case WRITE:
                    add(change);
                    break;
                case DELETE:
                    remove(change);
                    break;
            }
        }
    }

    public abstract void add(DataTreeModification<D> newDataObject);

    public abstract void remove(DataTreeModification<D> removedDataObject);

    public abstract void update(DataTreeModification<D> modifiedDataObject);

}
