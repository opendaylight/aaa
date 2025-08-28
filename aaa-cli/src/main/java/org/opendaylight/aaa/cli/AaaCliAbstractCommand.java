/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli;

import java.util.Collection;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;

/**
 * Base class for all CLI commands.
 *
 * @author mserngawy
 *
 */
@SuppressWarnings("checkstyle:RegexpSingleLineJava")
public abstract class AaaCliAbstractCommand implements Action {
    public static final String LOGIN_FAILED_MESS = "User does not exist OR user name and passsword are not correct";

    @Option(name = "-aaaAdmin",
            description = "AAA admin username",
            required = true,
            censor = true,
            multiValued = false)
    private String userName;

    @Option(name = "-aaaAdminPass",
            description = "AAA Admin password",
            required = true,
            censor = true,
            multiValued = false)
    private String passwd;

    @Reference protected IIDMStore identityStore;
    @Reference private PasswordHashService passwordService;

    @Override
    public Object execute() throws Exception {
        final User usr = DataStoreUtils.isAdminUser(identityStore, passwordService, userName, passwd);
        return usr;
    }

    protected void list(String name, Collection<?> items) {
        System.out.println(name);
        items.forEach(i -> System.out.println("  " + i));
    }
}
