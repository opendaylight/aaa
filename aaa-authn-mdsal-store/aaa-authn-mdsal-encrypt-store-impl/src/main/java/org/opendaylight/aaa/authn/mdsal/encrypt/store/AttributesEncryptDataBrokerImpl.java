/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 */

package org.opendaylight.aaa.authn.mdsal.encrypt.store;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;

/**
 * Created by sunikulk on 4/11/2016.
 */
public class AttributesEncryptDataBrokerImpl extends AttributesEncryptDataBroker {

    public AttributesEncryptDataBrokerImpl(final DataBroker dataBroker, final AttributeEncryptionMap attributeEncryptionMap) {
        super(dataBroker, attributeEncryptionMap);
    }
}
