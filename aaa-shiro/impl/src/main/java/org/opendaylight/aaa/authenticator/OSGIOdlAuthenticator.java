/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.authenticator;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import javax.servlet.http.HttpServletRequest;
import org.jolokia.osgi.security.Authenticator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@Component(immediate = true, service = Authenticator.class)
public class OSGIOdlAuthenticator extends ForwardingObject implements Authenticator {

    private ODLAuthenticator delegate;

    @Activate
    void activate() {
        delegate = new ODLAuthenticator();
    }

    @Deactivate
    void deactivate() {
        delegate = null;
    }

    @Override
    public boolean authenticate(HttpServletRequest httpServletRequest) {
        return delegate().authenticate(httpServletRequest);
    }

    @Override
    protected ODLAuthenticator delegate() {
        return delegate;
    }
}
