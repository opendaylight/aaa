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
import org.opendaylight.aaa.api.IIDMStore;
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.api.model.User;
import org.opendaylight.aaa.cli.AaaCliAbstractCommand;
import org.opendaylight.aaa.cli.utils.CliUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "add-domain", scope = "aaa", description = "Add domain.")

/**
 * @author mserngawy
 *
 */
public class AddDomain extends AaaCliAbstractCommand {

    private static final Logger LOG = LoggerFactory.getLogger(AddDomain.class);

    @Option(name = "-name",
            aliases = { "--domainName" },
            description = "The domain name",
            required = true,
            multiValued = false)
    private String domainName = "";

    @Option(name = "-desc",
            aliases = { "--domainDescription" },
            description = "The domain Description",
            required = true,
            multiValued = false)
    private String domainDesc = "";

    public AddDomain(final IIDMStore identityStore) {
        super(identityStore);
    }

    @Override
    protected Object doExecute() throws Exception {
        if (super.doExecute() == null) {
            return CliUtils.LOGIN_FAILED_MESS;
        }
        Domain domain = new Domain();
        domain.setDescription(domainDesc);
        domain.setEnabled(true);
        domain.setName(domainName);
        domain = identityStore.writeDomain(domain);
        if (domain != null) {
            return "Domain " + domainName + " has been created, Domain Id is " + domain.getDomainid();
        }
        return null;
    }

}
