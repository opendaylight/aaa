/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.dm.Component;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.ClientService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * A configuration-based client manager.
 *
 * @author liemmn
 *
 */
public class ClientManager implements ClientService, ManagedService {
    static final String CLIENTS = "authorizedClients";
    private static final String CLIENTS_FORMAT_ERR = "Clients are space-delimited in the form of <client_id>:<client_secret>";
    private static final String UNAUTHORIZED_CLIENT_ERR = "Unauthorized client";

    // Defaults (needed only for non-Karaf deployments)
    static final Dictionary<String, String> defaults = new Hashtable<>();
    static {
        defaults.put(CLIENTS, "dlux:secrete");
    }

    private final Map<String, String> clients = new ConcurrentHashMap<>();

    // This should be a singleton
    ClientManager() {
    }

    // Called by DM when all required dependencies are satisfied.
    void init(Component c) throws ConfigurationException {
        reconfig(defaults);
    }

    @Override
    public void validate(String clientId, String clientSecret)
            throws AuthenticationException {
        // TODO: Post-Helium, we will support a CRUD API
        if (!clients.containsKey(clientId))
            throw new AuthenticationException(UNAUTHORIZED_CLIENT_ERR);
        if (!clients.get(clientId).equals(clientSecret))
            throw new AuthenticationException(UNAUTHORIZED_CLIENT_ERR);
    }

    @Override
    public void updated(Dictionary<String, ?> props)
            throws ConfigurationException {
        if (props == null)
            props = defaults;
        reconfig(props);
    }

    // Reconfigure the client map...
    private void reconfig(@SuppressWarnings("rawtypes") Dictionary props)
            throws ConfigurationException {
        try {
            String authorizedClients = (String) props.get(CLIENTS);
            Map<String, String> newClients = new HashMap<>();
            if (authorizedClients != null) {
                for (String client : authorizedClients.split(" ")) {
                    String[] aClient = client.split(":");
                    newClients.put(aClient[0], aClient[1]);
                }
            }
            clients.clear();
            clients.putAll(newClients);
        } catch (Throwable t) {
            throw new ConfigurationException(null, CLIENTS_FORMAT_ERR);
        }
    }

}
