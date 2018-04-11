/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.web.test;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class TestServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(TestServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LOG.info("TestServlet init()");
    }

    @Override
    public void destroy() {
        super.destroy();
        LOG.info("TestServlet destroy()");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        response.getOutputStream().println("hello, world");
    }

}
