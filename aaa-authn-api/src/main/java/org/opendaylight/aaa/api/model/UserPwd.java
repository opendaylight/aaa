/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "userpwd")
public class UserPwd {
    private String username;
    private String userpwd;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String name) {
        username = name;
    }

    public String getUserpwd() {
        return userpwd;
    }

    public void setUserpwd(final String pwd) {
        userpwd = pwd;
    }
}
