/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
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
 * Removes a grant.
 *
 * @author mserngawy
 */
@Service
@Command(name = "remove-grant", scope = "aaa", description = "Remove grant.")
public class RemoveGrant implements Action {

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

    @Option(name = "-uname", aliases = {
            "--userName" }, description = "The user name", required = true, multiValued = false)
    private String userName;

    @Option(name = "-dname", aliases = {
            "--domainName" }, description = "The domain name", required = true, multiValued = false)
    private String domainName;

    @Option(name = "-rname", aliases = {
            "--roleName" }, description = "The role name", required = false, multiValued = false)
    private String roleName;

    @Override
    public Object execute() throws Exception {
        final User usr = DataStoreUtils.isAdminUser(identityStore, passwordService, adminUserName, adminUserPass);
        if (usr == null) {
            return CliUtils.LOGIN_FAILED_MESS;
        }

        final String grantid = DataStoreUtils.getGrantId(identityStore, domainName, roleName, userName);
        if (grantid == null) {
            return "Grant does not exist";
        }
        if (identityStore.deleteGrant(grantid) == null) {
            return "Failed to delete grant " + userName + " " + roleName + " " + domainName;
        }
        return "Grant has been deleted.";
    }
}
