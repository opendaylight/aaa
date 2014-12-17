/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.hp.demo.plugable.persistence.model;

/**
 * Application model or business logic API.
 * 
 * @author Fabiel Zuniga
 */
public interface Model {

    /**
     * Gets the network device service.
     * 
     * @return the network device service
     */
    public NetworkDeviceService getNetworkDeviceService();

    /**
     * Gets the user service.
     * 
     * @return the user service
     */
    public UserService getUserService();
}
