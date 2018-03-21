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
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.api.ClaimCache;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.SHA256Calculator;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.model.Users;
import org.opendaylight.aaa.cli.utils.CliUtils;

@Command(name = "change-user-pwd", scope = "aaa", description = "Change the user password.")

/**
 * ChangeUserPassword change the user password.
 *
 * @author mserngawy
 *
 */
public class ChangeUserPassword extends OsgiCommandSupport {

    private final IIDMStore identityStore;
    private final ClaimCache claimCache;

    @Option(name = "-user", aliases = {
            "--userName" }, description = "The user name", required = true, multiValued = false)
    private String userName;

    public ChangeUserPassword(IIDMStore identityStore, ClaimCache claimCache) {
        this.identityStore = identityStore;
        this.claimCache = claimCache;
    }

    @Override
    protected Object doExecute() throws Exception {
        if (identityStore == null) {
            return "Failed to access the users data store";
        }
        final String currentPwd = CliUtils.readPassword(this.session, "Enter current password:");
        final String newPwd = CliUtils.readPassword(this.session, "Enter new password:");
        final Users users = identityStore.getUsers();
        for (User usr : users.getUsers()) {
            final String realPwd = SHA256Calculator.getSHA256(currentPwd, usr.getSalt());
            if (usr.getName().equals(userName) && usr.getPassword().equals(realPwd)) {
                claimCache.clear();
                usr.setPassword(newPwd);
                identityStore.updateUser(usr);
                return userName + "'s password has been changed";
            }
        }
        return CliUtils.LOGIN_FAILED_MESS;
    }
}
