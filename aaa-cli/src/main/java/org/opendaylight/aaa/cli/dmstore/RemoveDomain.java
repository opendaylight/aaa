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
import org.opendaylight.aaa.cli.utils.DataStoreUtils;

@Command(name = "remove-domain", scope = "aaa", description = "Remove domain.")

/**
 * @author mserngawy
 *
 */
public class RemoveDomain extends OsgiCommandSupport {

    protected IIDMStore identityStore;

    @Option(name = "-name",
            aliases = { "--domainName" },
            description = "The domain name",
            required = true,
            multiValued = false)
    private String domainName = "";

    public RemoveDomain(final IIDMStore identityStore) {
        this.identityStore = identityStore;
    }

    @Override
    protected Object doExecute() throws Exception {
        final String domainId = DataStoreUtils.getDomainId(identityStore, domainName);
        if (domainId == null) {
            return "User does not exist";
        }
        if (identityStore.deleteDomain(domainId) == null) {
            return "Failed to delete domain " + domainName;
        }
        return "Domain " + domainName + "has been deleted.";
    }
}
