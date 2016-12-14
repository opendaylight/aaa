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
import org.opendaylight.aaa.cli.AaaCliAbstractCommand;
import org.opendaylight.aaa.cli.utils.CliUtils;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;

@Command(name = "remove-grant", scope = "aaa", description = "Remove grant.")

/**
 * @author mserngawy
 *
 */
public class RemoveGrant extends AaaCliAbstractCommand {

    @Option(name = "-uname",
            aliases = { "--userName" },
            description = "The user name",
            required = true,
            multiValued = false)
    private String userName = "";

    @Option(name = "-dname",
            aliases = { "--domainName" },
            description = "The domain name",
            required = true,
            multiValued = false)
    private String domainName = "";

    @Option(name = "-rname",
            aliases = { "--roleName" },
            description = "The role name",
            required = false,
            multiValued = false)
    private String roleName = "";

    public RemoveGrant(final IIDMStore identityStore) {
        super(identityStore);
    }

    @Override
    protected Object doExecute() throws Exception {
        if (super.doExecute() == null) {
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
