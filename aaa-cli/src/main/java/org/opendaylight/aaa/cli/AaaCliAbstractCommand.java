/*
 * Copyright (c) 2016 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.cli;

import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.cli.utils.CliUtils;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;

public abstract class AaaCliAbstractCommand extends OsgiCommandSupport {

    private volatile static String authUser = null;
    protected final IIDMStore identityStore;

    public AaaCliAbstractCommand(IIDMStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    protected Object doExecute() throws Exception {
        User currentUser = SessionsManager.getInstance().getCurrentUser(authUser);
        if (currentUser == null) {
            final String userName = CliUtils.readPassword(this.session, "Enter Username:");
            final String passwd = CliUtils.readPassword(this.session, "Enter Password:");
            final User usr = DataStoreUtils.isAdminUser(identityStore, userName, passwd);
            if(usr != null) {
                authUser = userName;
                SessionsManager.getInstance().addUserSession(userName, usr);
            }
            return usr;
        }
        return currentUser;
    }
}
