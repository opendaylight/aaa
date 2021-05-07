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
import org.opendaylight.aaa.api.model.Domain;
import org.opendaylight.aaa.cli.AaaCliAbstractCommand;

/**
 * Adds a domain.
 *
 * @author mserngawy
 */
@Service
@Command(name = "add-domain", scope = "aaa", description = "Add domain.")
public class AddDomain extends AaaCliAbstractCommand {
    @Option(name = "-name",
            aliases = { "--domainName" },
            description = "The domain name",
            required = true,
            multiValued = false)
    private String domainName;

    @Option(name = "-desc",
            aliases = { "--domainDescription" },
            description = "The domain Description",
            required = true,
            multiValued = false)
    private String domainDesc;

    @Override
    public Object execute() throws Exception {
        if (super.execute() == null) {
            return LOGIN_FAILED_MESS;
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
