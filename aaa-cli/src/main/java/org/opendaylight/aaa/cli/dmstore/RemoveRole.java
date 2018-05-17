/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.dmstore;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.opendaylight.aaa.api.ClaimCache;
import org.opendaylight.aaa.api.password.service.PasswordService;
import org.opendaylight.aaa.cli.AaaCliAbstractCommand;
import org.opendaylight.aaa.cli.utils.CliUtils;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;

@Command(name = "remove-role", scope = "aaa", description = "Remove role.")

/**
 * @author mserngawy
 *
 */
public class RemoveRole extends AaaCliAbstractCommand {

    private final ClaimCache claimCache;

    @Option(name = "-name", aliases = {
            "--roleName" }, description = "The role name", required = true, multiValued = false)
    private String roleName;

    public RemoveRole(final ClaimCache claimCache, final PasswordService passwordService) {
        super(passwordService);
        this.claimCache = claimCache;
    }

    @Override
    protected Object doExecute() throws Exception {
        if (super.doExecute() == null) {
            return CliUtils.LOGIN_FAILED_MESS;
        }
        final String roleId = DataStoreUtils.getRoleId(identityStore, roleName);
        if (roleId == null) {
            return "Role does not exist";
        }
        if (identityStore.deleteRole(roleId) == null) {
            return "Failed to delete role " + roleName;
        }
        claimCache.clear();
        return "Role " + roleName + "has been deleted.";
    }
}
