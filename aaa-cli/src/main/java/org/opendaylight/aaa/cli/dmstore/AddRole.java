/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli.dmstore;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.StoreBuilder;
import org.opendaylight.aaa.api.model.Role;
import org.opendaylight.aaa.cli.AaaCliAbstractCommand;
import org.opendaylight.aaa.cli.utils.CliUtils;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;

@Command(name = "add-role", scope = "aaa", description = "Add role.")

/**
 * @author mserngawy
 *
 */
public class AddRole extends AaaCliAbstractCommand {

    @Option(name = "-name",
            aliases = { "--roleName" },
            description = "The role name",
            required = true,
            multiValued = false)
    private String roleName = "";

    @Option(name = "-dname",
            aliases = { "--domainName" },
            description = "The domain name",
            required = true,
            multiValued = false)
    private String domainName = "";

    @Option(name = "-desc",
            aliases = { "--roleDescription" },
            description = "The role Description",
            required = true,
            multiValued = false)
    private String roleDesc = "";

    public AddRole(final IIDMStore identityStore) {
        super(identityStore);
    }

    @Override
    protected Object doExecute() throws Exception {
        if (super.doExecute() == null) {
            return CliUtils.LOGIN_FAILED_MESS;
        }
        Role role = new Role();
        role.setDescription(roleDesc);
        role.setName(roleName);
        final String domainId = DataStoreUtils.getDomainId(identityStore, domainName);
        if (domainId == null) {
            return "Domain does not exist";
        }
        role.setDomainid(domainId);
        role = identityStore.writeRole(role);
        if (role != null) {
            return "Role " + roleName + " has been created, Role Id is " + role.getRoleid();
        }
        return null;
    }

}
