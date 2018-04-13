/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.servlet.jersey2;

import com.google.common.annotations.Beta;
import javax.annotation.concurrent.ThreadSafe;
import javax.ws.rs.core.Application;
import org.opendaylight.aaa.web.servlet.HttpServletBuilder;
import org.opendaylight.aaa.web.servlet.HttpServletBuilderFactory;

@Beta
@ThreadSafe
public final class JerseyHttpServletBuilderFactory implements HttpServletBuilderFactory {

    @Override
    public HttpServletBuilder builderForApplication(final Application application) {
        return new JerseyHttpServletBuilder(application);
    }
}
