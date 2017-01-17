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
import org.opendaylight.aaa.api.model.Grant;
import org.opendaylight.aaa.cli.AaaCliAbstractCommand;
import org.opendaylight.aaa.cli.utils.CliUtils;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;

@Command(name = "add-grant", scope = "aaa", description = "Add Grant.")

/**
 * @author mserngawy
 *
 */
public class AddGrant extends AaaCliAbstractCommand {

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


    public AddGrant(final IIDMStore identityStore) {
        super(identityStore);
    }

    @Override
    protected Object doExecute() throws Exception {
        if (super.doExecute() == null) {
            return CliUtils.LOGIN_FAILED_MESS;
        }
        final String domainId = DataStoreUtils.getDomainId(identityStore, domainName);
        if (domainId == null) {
            return "Domain does not exist";
        }
        final String roleId = DataStoreUtils.getRoleId(identityStore, roleName);
        if (roleId == null) {
            return "Role does not exist";
        }
        final String usrId = DataStoreUtils.getUserId(identityStore, userName);
        if (usrId == null) {
            return "User does not exist";
        }
        Grant grant = new Grant();
        grant.setDomainid(domainId);
        grant.setRoleid(roleId);
        grant.setUserid(usrId);
        grant = identityStore.writeGrant(grant);
        if (grant != null) {
            return "Grant has been created, Grant id is " + grant.getGrantid();
        }
        return null;
    }
}
