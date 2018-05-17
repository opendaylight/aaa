/*
 * Copyright (c) 2016, 2017 Inocybe Technologies. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.cli;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.api.password.service.PasswordHashService;
import org.opendaylight.aaa.cli.utils.CliUtils;
import org.opendaylight.aaa.cli.utils.DataStoreUtils;

/**
 * Base class for all CLI commands.
 *
 * @author mserngawy
 *
 */
@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
public abstract class AaaCliAbstractCommand extends OsgiCommandSupport {

    private static volatile String authUser = null;
    protected IIDMStore identityStore;
    private final PasswordHashService passwordService;

    public AaaCliAbstractCommand(final PasswordHashService passwordService) {
        Objects.requireNonNull(this.passwordService = passwordService);
    }

    public void setIdentityStore(IIDMStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    protected Object doExecute() throws Exception {
        final User currentUser = SessionsManager.getInstance().getCurrentUser(authUser);
        if (currentUser == null) {
            final String userName = CliUtils.readPassword(super.session, "Enter Username:");
            final String passwd = CliUtils.readPassword(super.session, "Enter Password:");
            final User usr = DataStoreUtils.isAdminUser(identityStore, passwordService, userName, passwd);
            if (usr != null) {
                authUser = userName;
                SessionsManager.getInstance().addUserSession(userName, usr);
            }
            return usr;
        }
        return currentUser;
    }
}
