/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.dmstore;

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
 * ListODLDomains list the available domains at ODL aaa data store.
 *
 * @author mserngawy
 */
@Service
@Command(name = "get-domains", scope = "aaa", description = "get list of ODL domains.")
public class ListODLDomains implements Action {

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

        CliUtils.list("Domains: ", identityStore.getDomains().getDomains());
        return null;
    }
}
