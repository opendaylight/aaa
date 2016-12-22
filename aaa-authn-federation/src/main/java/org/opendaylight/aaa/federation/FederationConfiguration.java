/*
 * Copyright (c) 2014, 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.aaa.federation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * AAA federation configurations in OSGi.
 *
 * @author liemmn
 *
 */
@Deprecated
public class FederationConfiguration implements ManagedService {
    private static final String FEDERATION_CONFIG_ERR = "Error saving federation configuration";

    static final String HTTP_HEADERS = "httpHeaders";
    static final String HTTP_ATTRIBUTES = "httpAttributes";
    static final String SECURE_PROXY_PORTS = "secureProxyPorts";

    static FederationConfiguration instance = new FederationConfiguration();

    static final Hashtable<String, String> defaults = new Hashtable<>();
    static {
        defaults.put(HTTP_HEADERS, "");
        defaults.put(HTTP_ATTRIBUTES, "");
    }
    private static Map<String, String> configs = new ConcurrentHashMap<>();

    // singleton
    private FederationConfiguration() {
    }

    public static FederationConfiguration instance() {
        return instance;
    }

    @Override
    public void updated(Dictionary<String, ?> props) throws ConfigurationException {
        if (props == null) {
            configs.clear();
            configs.putAll(defaults);
        } else {
            try {
                Enumeration<String> keys = props.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    configs.put(key, (String) props.get(key));
                }
            } catch (Throwable t) {
                throw new ConfigurationException(null, FEDERATION_CONFIG_ERR, t);
            }
        }
    }

    public List<String> httpHeaders() {
        String headers = configs.get(HTTP_HEADERS);
        return (headers == null) ? new ArrayList<String>() : Arrays.asList(headers.split(" "));
    }

    public List<String> httpAttributes() {
        String attributes = configs.get(HTTP_ATTRIBUTES);
        return (attributes == null) ? new ArrayList<String>() : Arrays
                .asList(attributes.split(" "));
    }

    public Set<Integer> secureProxyPorts() {
        String ports = configs.get(SECURE_PROXY_PORTS);
        Set<Integer> secureProxyPorts = new TreeSet<Integer>();

        if (ports != null && !ports.isEmpty()) {
            for (String port : ports.split(" ")) {
                secureProxyPorts.add(Integer.parseInt(port));
            }
        }
        return secureProxyPorts;
    }

}
