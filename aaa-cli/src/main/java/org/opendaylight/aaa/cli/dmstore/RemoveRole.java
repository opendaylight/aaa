/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.dmstore;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.aaa.cli.AaaCliAbstractCommand;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;

/**
 * Removes a role.
 *
 * @author mserngawy
 */
@Service
@Command(name = "remove-role", scope = "aaa", description = "Remove role.")
public class RemoveRole extends AaaCliAbstractCommand {
    @Option(name = "-name",
            aliases = { "--roleName" },
            description = "The role name",
            required = true,
            multiValued = false)
    private String roleName;

    @Override
    public Object execute() throws Exception {
        if (super.execute() == null) {
            return LOGIN_FAILED_MESS;
        }
        final String roleId = DataStoreUtils.getRoleId(identityStore, roleName);
        if (roleId == null) {
            return "Role does not exist";
        }
        if (identityStore.deleteRole(roleId) == null) {
            return "Failed to delete role " + roleName;
        }
        return "Role " + roleName + "has been deleted.";
    }
}
