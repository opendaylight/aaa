/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.aaa.thirdparty.authn;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
/**
 * @author - Sharon Aicler (saichler@cisco.com)
 **/
public class RadiusConfiguration implements ManagedService {

    private static RadiusConfiguration instance = new RadiusConfiguration();
    private Dictionary<String, Object> props = new Hashtable<String, Object>();

    public static final String PROP_HOST = "radius-host";
    public static final String PROP_SECRET = "radius-secret";
    public static final String PROP_ENABLED = "radius-enable";

    private RadiusConfiguration() {
        this.props.put(PROP_ENABLED, new Boolean(false));
    }

    public static RadiusConfiguration getInstance() {
        return instance;
    }

    @Override
    public void updated(Dictionary<String, ?> properties)
            throws ConfigurationException {
        if (properties != null && !properties.isEmpty()) {
            Enumeration<String> k = properties.keys();
            while (k.hasMoreElements()) {
                String key = k.nextElement();
                String value = (String) properties.get(key);
                props.put(key, value);
            }
        }
    }

    public Dictionary<String, Object> getConfiguration() {
        return this.props;
    }

    public String getHost() {
        return (String)this.props.get(PROP_HOST);
    }

    public String getSECRET() {
        return (String)this.props.get(PROP_SECRET);
    }

    public boolean isEnabled() {
        return Boolean.parseBoolean(this.props.get(PROP_ENABLED).toString());
    }

    public void setEnabled(boolean b){
        this.props.put(PROP_ENABLED, b);
    }
}