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
public class LDAPConfiguration implements ManagedService {

    private static LDAPConfiguration instance = new LDAPConfiguration();
    private Dictionary<String, Object> ldapProperties = new Hashtable<String, Object>();

    public static final String PROP_HOST = "ldap-host";
    public static final String PROP_SSL_PORT = "ldap-ssl-port";
    public static final String PROP_NOSSL_PORT = "ldap-nossl-port";
    public static final String PROP_DN = "ldap-dn";
    public static final String PROP_ENABLED = "ldap-enable";
    public static final String PROP_USERSSL = "ldap-use-ssl";
    public static final String PROP_OBJECT_GROUP = "ldap-object-group";
    public static final String PROP_TIMEOUT = "ldap-timeout";

    private LDAPConfiguration() {
        this.ldapProperties.put(PROP_ENABLED, new Boolean(false));
    }

    public static LDAPConfiguration getInstance() {
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
                ldapProperties.put(key, value);
            }
        }
    }

    public Dictionary<String, Object> getConfiguration() {
        return this.ldapProperties;
    }

    public String getHost() {
        return (String)this.ldapProperties.get(PROP_HOST);
    }

    public boolean isSSLEnabled() {
        return Boolean.parseBoolean(this.ldapProperties.get(PROP_USERSSL).toString());
    }

    public int getSSLPort() {
        return Integer.parseInt(ldapProperties.get(PROP_SSL_PORT).toString());
    }

    public int getNOSSLPort() {
        return Integer.parseInt(ldapProperties.get(PROP_NOSSL_PORT).toString());
    }

    public String getDN() {
        return (String)this.ldapProperties.get(PROP_DN);
    }

    public boolean isEnabled() {
        return Boolean.parseBoolean(this.ldapProperties.get(PROP_ENABLED).toString());
    }

    public void setEnabled(boolean b){
        this.ldapProperties.put(PROP_ENABLED, b);
    }

    public String getObjectGroup() {
        return (String)this.ldapProperties.get(PROP_OBJECT_GROUP);
    }

    public long getLDAPTimeout(){
        return Long.parseLong(this.ldapProperties.get(PROP_TIMEOUT).toString());
    }
}