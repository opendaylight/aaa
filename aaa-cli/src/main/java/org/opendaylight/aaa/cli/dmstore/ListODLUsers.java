/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.dmstore;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.cli.utils.CliUtils;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;

/**
 * ListODLDomains list the available users at ODL aaa data store.
 *
 * @author mserngawy
 */
@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
@SuppressWarnings("checkstyle:RegexpSingleLineJava")
@Service
@Command(name = "get-users", scope = "aaa", description = "get list of ODL users.")
public class ListODLUsers implements Action {

    @Reference protected IIDMStore identityStore;
    @Reference private PasswordHashService passwordService;


    @Option(name = "-aaaAdmin",
            aliases = { "--aaaAdminUserName" },
            description = "AAA admin user name",
            required = true,
            censor = true,
            multiValued = false)
    private String adminUserName;

    @Option(name = "-aaaAdminPass",
            aliases = { "--aaaAdminPassword" },
            description = "AAA admin password",
            required = true,
            censor = true,
            multiValued = false)
    private String adminUserPass;

    @Override
    public Object execute() throws Exception {
        final User usr = DataStoreUtils.isAdminUser(identityStore, passwordService, adminUserName, adminUserPass);
        if (usr == null) {
            return CliUtils.LOGIN_FAILED_MESS;
        }

        CliUtils.list("Users: ", identityStore.getUsers().getUsers());
        return null;
    }
}
