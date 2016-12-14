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
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.cli.AaaCliAbstractCommand;
import org.opendaylight.aaa.cli.utils.CliUtils;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;

@Command(name = "add-user", scope = "aaa", description = "Add user.")

/**
 * @author mserngawy
 *
 */
public class AddUser extends AaaCliAbstractCommand {

    @Option(name = "-name",
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

    @Option(name = "-desc",
            aliases = { "--userDescription" },
            description = "The user Description",
            required = false,
            multiValued = false)
    private String userDesc = "";

    @Option(name = "-email",
            aliases = { "--userEmail" },
            description = "The user Description",
            required = false,
            multiValued = false)
    private String userEmail = "";

    public AddUser(final IIDMStore identityStore) {
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
        User usr = new User();
        usr.setDescription(userDesc);
        usr.setDomainid(domainId);
        usr.setEnabled(true);
        usr.setEmail(userEmail);
        final String pwd = CliUtils.readPassword(this.session, "Enter new user password: ");
        if (pwd == null || pwd.isEmpty() || pwd.length() < 6) {
            return "Password should be at least 6 characters";
        }
        usr.setPassword(pwd);
        usr.setName(userName);
        usr = identityStore.writeUser(usr);
        if (usr != null) {
            if (roleName != null && !roleName.isEmpty()) {
                final String roleId = DataStoreUtils.getRoleId(identityStore, roleName);
                if (roleId == null) {
                    return "User " + userName + " has been created, User Id is " + usr.getUserid() + ", Role does not exist";
                }
                Grant grant = new Grant();
                grant.setDomainid(domainId);
                grant.setRoleid(roleId);
                grant.setUserid(usr.getUserid());
                grant = identityStore.writeGrant(grant);
                if (grant == null) {
                    return "User " + usr.getName() + " has been created, User Id is " + usr.getUserid()
                                    + " but not granted to role " + roleName;
                }
                return "User " + usr.getName() + " has been created, User Id is " + usr.getUserid()
                                    + " and granted to role " + roleName;
            }
            return "User " + userName + " has been created, User Id is " + usr.getUserid();
        }
        return null;
    }
}
